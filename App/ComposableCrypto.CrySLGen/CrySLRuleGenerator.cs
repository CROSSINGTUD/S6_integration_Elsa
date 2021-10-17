using ComposableCrypto.Core;
using System;
using System.Collections.Generic;
using System.Data.HashFunction;
using System.Data.HashFunction.CityHash;
using System.Linq;

namespace ComposableCrypto.CrySLGen
{
    public class CrySLRuleGenerator
    {
        private static readonly ICityHash city = CityHashFactory.Instance.Create();
        private readonly Component rootComponent;
        private readonly IDictionary<ComponentDescription, CrySLRule> rules = new Dictionary<ComponentDescription, CrySLRule>();

        public CrySLRuleGenerator(Component rootComponent)
        {
            this.rootComponent = rootComponent;
        }

        public ICollection<Tuple<string, string>> GenerateCrySLRules()
        {
            Generate(null, rootComponent);
            return rules.Values.Select(rule => Tuple.Create(rule.FileName, rule.ToString())).ToList();
        }

        private void Generate(string edgeHash, Component component)
        {
            // Fetch or create CrySL rule
            if (!rules.TryGetValue(component.Description, out CrySLRule rule))
            {
                rule = new CrySLRule(TypeRegistry.ClassNameForComponent(component.Description.Name));
                rules.Add(component.Description, rule);
            }

            rule.ObjectsSection.AppendFormat("\t{0} obj{1};", rule.TypeName, edgeHash);
            rule.ObjectsSection.AppendLine();
            foreach (KeyValuePair<string, Component> child in component.Children)
            {
                //rule.ObjectsSection.AppendFormat("\t{0} obj{1};", TypeRegistry.InterfaceNameForComponentType(child.Value.Description.Type), city.ComputeHash(edgeHash + child.Key).AsHexString());
                rule.ObjectsSection.AppendFormat("\t{0} obj{1};", TypeRegistry.ClassNameForComponent(child.Value.Description.Name), city.ComputeHash(edgeHash + child.Key).AsHexString());
                rule.ObjectsSection.AppendLine();
            }

            rule.EventsSection.AppendFormat("\tg{0}: obj{0} = createInstance(", edgeHash);
            rule.EventsSection.AppendJoin(", ", component.Children.Keys.Select(k => "obj" + city.ComputeHash(edgeHash + k).AsHexString()));
            rule.EventsSection.AppendLine(");");

            rule.Events.Add("g" + edgeHash);

            foreach (KeyValuePair<string, Component> child in component.Children)
            {
                rule.RequiresSection.AppendFormat("\tedge{0}[obj{0}];", city.ComputeHash(edgeHash + child.Key).AsHexString());
                rule.RequiresSection.AppendLine();
            }

            if (edgeHash is not null)
            {
                rule.EnsuresSection.AppendFormat("\tedge{0}[obj{0}] after g{0};", edgeHash);
                rule.EnsuresSection.AppendLine();
            }

            foreach (KeyValuePair<string, Component> child in component.Children)
            {
                Generate(city.ComputeHash(edgeHash + child.Key).AsHexString(), child.Value);
            }
        }
    }
}
