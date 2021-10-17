using System;
using System.Collections.Generic;
using System.Collections.Immutable;
using System.IO;
using System.Linq;
using System.Text;
using System.Text.Json;
using System.Threading.Tasks;

namespace ComposableCrypto.Core
{
    public class Component
    {
        private readonly ICollection<ComponentDescription> parentComponents;
        public Component Parent { get; }
        public ComponentDescription Description { get; }
        public IDictionary<string, Component> Children { get; } = new SortedDictionary<string, Component>();
        public ICollection<string> RequiredProperties { get; set; } = new HashSet<string>();

        public Component(ComponentDescription description, Component parent)
        {
            Description = description;
            Parent = parent;
            parentComponents = new List<ComponentDescription>();
            for (Component c = Parent; c is not null; c = c.Parent)
            {
                parentComponents.Add(c.Description);
            }
            if (parent is null)
            {
                foreach (string propertyName in Description.Properties.Keys)
                {
                    RequiredProperties.Add(propertyName);
                }
            }
        }

        public static async Task<Component> FromJsonAsync(Stream json)
        {
            using JsonDocument jsonDocument = await JsonDocument.ParseAsync(json);
            return CreateFromJson(null, jsonDocument.RootElement);
        }

        protected static Component CreateFromJson(Component parent, JsonElement jsonElement)
        {
            string name = jsonElement.GetProperty("name").GetString();
            Component component = ComponentRegistry.CreateComponent(name, parent);
            foreach (JsonProperty child in jsonElement.GetProperty("children").EnumerateObject())
            {
                component.Children.Add(child.Name, CreateFromJson(component, child.Value));
            }
            return component;
        }

        public bool EvaluateProperty(string propertyName)
        {
            return Description.GetPropertyOrFalse(propertyName).Evaluate(this);
        }

        public bool AutoComplete() => AutoComplete(Array.Empty<string>());

        public bool AutoComplete(ICollection<string> requiredProperties) => AutoComplete(requiredProperties, Array.Empty<string>());

        public bool AutoComplete(ICollection<string> requiredProperties, ICollection<string> requiredNegProperties)
        {
            IImmutableStack<string> unsetChildren = ImmutableStack.CreateRange(Description.Children.Keys.Except(Children.Keys));

            IList<Condition> requiredConditions = new List<Condition>();
            foreach (string requiredProperty in RequiredProperties.Union(requiredProperties))
            {
                if (Description.Properties.TryGetValue(requiredProperty, out Condition requiredCondition))
                {
                    requiredConditions.Add(requiredCondition);
                }
                else
                {
                    Console.WriteLine($"Property {requiredProperty} is required for component {Description.DisplayName}, but there is no definition of this property.");
                    return false;
                }
            }
            Condition requirement = And.Of(requiredConditions);
            ICollection<Condition> alternatives = PropositionalLogicUtils.CollectAlternatives(requirement);
            foreach (Condition alternative in alternatives)
            {
                (IDictionary<string, ICollection<string>> childrenRequirements, IDictionary<string, ICollection<string>> childrenNegRequirements) = PropositionalLogicUtils.CollectRequiredChildrenProperties(alternative);
                foreach ((string childIdentifier, Component child) in Children)
                {
                    if (!childrenRequirements.TryGetValue(childIdentifier, out ICollection<string> requirements))
                    {
                        requirements = Array.Empty<string>();
                    }
                    if (!childrenNegRequirements.TryGetValue(childIdentifier, out ICollection<string> negRequirements))
                    {
                        negRequirements = Array.Empty<string>();
                    }
                    if (!child.AutoComplete(requirements, negRequirements))
                    {
                        goto REPEAT;
                    }
                }
                if (AutoCompleteHelper(childrenRequirements, childrenNegRequirements, unsetChildren) && requiredProperties.All(p => Description.Properties[p].Evaluate(this)) && requiredNegProperties.All(p => !Description.Properties[p].Evaluate(this)))
                {
                    return true;
                }

                REPEAT:;
            }

            foreach (string unsetChild in unsetChildren)
            {
                Children.Remove(unsetChild);
            }

            return false;
        }

        private bool AutoCompleteHelper(IDictionary<string, ICollection<string>> childrenRequirements, IDictionary<string, ICollection<string>> childrenNegRequirements, IImmutableStack<string> unsetChildren)
        {
            if (unsetChildren.IsEmpty)
            {
                return true;
            }

            string childName = unsetChildren.Peek();
            if (!childrenRequirements.TryGetValue(childName, out ICollection<string> requirements))
            {
                requirements = Array.Empty<string>();
            }
            if (!childrenNegRequirements.TryGetValue(childName, out ICollection<string> negRequirements))
            {
                negRequirements = Array.Empty<string>();
            }
            IEnumerable<ComponentDescription> candidates = ComponentRegistry.GetDescriptionsForType(Description.Children[childName].Type).Where(c => !parentComponents.Contains(c) && Description != c && !requirements.Except(c.Properties.Keys).Any());
            foreach (ComponentDescription candidate in candidates)
            {
                Component component = ComponentRegistry.CreateComponent(candidate, this);
                if (component.AutoComplete(requirements, negRequirements))
                {
                    Children[childName] = component;
                    return AutoCompleteHelper(childrenRequirements, childrenNegRequirements, unsetChildren.Pop());
                }
            }

            return false;
        }

        public bool AutoFixup()
        {
            List<string> populatedKeys = new(Children.Keys);
            for (int i = 0; i <= populatedKeys.Count; i++)
            {
                if (AutoFixup(populatedKeys, i, 0))
                {
                    return true;
                }
            }
            return false;
        }

        private bool AutoFixup(List<string> populatedKeys, int amountOfChildrenToRemove, int index)
        {
            if (amountOfChildrenToRemove == 0)
            {
                return AutoComplete();
            }
            else if (populatedKeys.Count - index < amountOfChildrenToRemove)
            {
                return false;
            }
            else
            {
                string key = populatedKeys[index];
                _ = Children.Remove(key, out Component child);
                if (AutoFixup(populatedKeys, amountOfChildrenToRemove - 1, index + 1))
                {
                    return true;
                }
                else
                {
                    Children.Add(key, child);
                    return AutoFixup(populatedKeys, amountOfChildrenToRemove, index + 1);
                }
            }
        }

        public string ToJson()
        {
            using MemoryStream stream = new();
            using Utf8JsonWriter writer = new(stream, new JsonWriterOptions
            {
                Indented = true
            });
            writer.WriteStartObject();
            WriteJson(writer);
            writer.WriteEndObject();
            writer.Flush();
            return Encoding.UTF8.GetString(stream.ToArray());
        }

        protected void WriteJson(Utf8JsonWriter writer)
        {
            writer.WriteString("name", Description.Name);
            writer.WriteStartObject("children");
            foreach ((string childName, Component child) in Children)
            {
                writer.WriteStartObject(childName);
                child.WriteJson(writer);
                writer.WriteEndObject();
            }
            writer.WriteEndObject();
        }
    }
}
