using ComposableCrypto.CrySLGen;
using Microsoft.JSInterop;
using System;
using System.IO;
using System.Linq;
using System.Reflection.Metadata;
using System.Text.Json;
using System.Threading.Tasks;

namespace ComposableCrypto.Core
{
    public static class ComposableCryptoImport
    {
        [JSInvokable]
        public static void ImportAssumptionProfile(string json) => AssumptionRegistry.RegisterAssumptionProfile(json);

        [JSInvokable]
        public static void ImportComponentDescriptions(string json)
        {
            using JsonDocument document = JsonDocument.Parse(json);
            ImportComponentDescriptions(document);
        }

        public static async Task ImportComponentDescriptionsAsync(Stream json)
        {
            using JsonDocument document = await JsonDocument.ParseAsync(json);
            ImportComponentDescriptions(document);
        }

        private static void ImportComponentDescriptions(JsonDocument document)
        {
            JsonElement root = document.RootElement;

            if (root.TryGetProperty("interfaces", out JsonElement interfaces))
            {
                foreach (JsonProperty prop in interfaces.EnumerateObject())
                {
                    TypeRegistry.RegisterInterface(prop.Name, prop.Value.GetString());
                }
            }

            if (root.TryGetProperty("components", out JsonElement components))
            {
                foreach (JsonElement component in components.EnumerateArray())
                {
                    ComponentDescriptionBuilder builder = ComponentDescription.Builder();
                    string name = component.GetProperty("name").GetString();
                    builder.Name(name);
                    builder.Type(component.GetProperty("type").GetString());
                    builder.DisplayName(component.GetProperty("displayName").GetString());
                    if (component.TryGetProperty("children", out JsonElement children))
                    {
                        foreach (JsonProperty child in children.EnumerateObject())
                        {
                            builder.WithChild(child.Name,
                                child.Value.GetProperty("displayName").GetString(),
                                child.Value.GetProperty("type").GetString());
                        }
                    }
                    if (component.TryGetProperty("properties", out JsonElement properties))
                    {
                        foreach (JsonProperty property in properties.EnumerateObject())
                        {
                            builder.WithProperty(property.Name, ParseCondition(property.Value));
                        }
                    }
                    ComponentRegistry.RegisterComponent(builder.Build());
                    TypeRegistry.RegisterComponentClass(name, component.GetProperty("javaClass").GetString());
                    Console.WriteLine($"Registered component {name}");
                }
            }
        }

        private static Condition ParseCondition(JsonElement json)
        {
            switch (json.ValueKind)
            {
                case JsonValueKind.Object:
                    if (json.EnumerateObject().Count() == 1)
                    {
                        if (json.TryGetProperty("and", out JsonElement conjunction) && conjunction.ValueKind == JsonValueKind.Array)
                        {
                            return And.Of(conjunction.EnumerateArray().Select(c => ParseCondition(c)));
                        }
                        else if (json.TryGetProperty("or", out JsonElement disjunction) && disjunction.ValueKind == JsonValueKind.Array)
                        {
                            return Or.Of(disjunction.EnumerateArray().Select(c => ParseCondition(c)));
                        }
                        else if (json.TryGetProperty("not", out JsonElement negatedCondition))
                        {
                            return new Not(ParseCondition(negatedCondition));
                        }
                    }
                    break;
                case JsonValueKind.String:
                    string condition = json.GetString();
                    if (condition.StartsWith("assumption:"))
                    {
                        string assumption = condition["assumption:".Length..];
                        AssumptionRegistry.RegisterAssumption(assumption);
                        return new AssumptionCondition(assumption);
                    }
                    else
                    {
                        int index = condition.IndexOf('.');
                        return new ChildPropertyCondition(condition.Substring(0, index), condition[(index + 1)..]);
                    }
                case JsonValueKind.True:
                    return Condition.TRUE;
                case JsonValueKind.False:
                    return Condition.FALSE;
                default:
                    break;
            }
            throw new ArgumentException("The specified condition is invalid.");
        }
    }
}
