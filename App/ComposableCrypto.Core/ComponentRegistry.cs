using System.Collections.Generic;
using System.Collections.ObjectModel;

namespace ComposableCrypto.Core
{
    public class ComponentRegistry
    {
        private static readonly IDictionary<string, ComponentDescription> descriptions = new Dictionary<string, ComponentDescription>();
        private static readonly IDictionary<string, IList<ComponentDescription>> descriptionsForType = new Dictionary<string, IList<ComponentDescription>>();

        public static void RegisterComponent(ComponentDescription description)
        {
            descriptions.Add(description.Name, description);
            if (!descriptionsForType.ContainsKey(description.Type))
            {
                descriptionsForType.Add(description.Type, new List<ComponentDescription>());
            }
            descriptionsForType[description.Type].Add(description);
        }

        public static Component CreateComponent(string name, Component parent)
        {
            return new Component(descriptions[name], parent);
        }

        public static Component CreateComponent(ComponentDescription description, Component parent)
        {
            return new Component(description, parent);
        }

        public static ICollection<ComponentDescription> GetDescriptionsForType(string type)
        {
            if (type is null)
            {
                return descriptions.Values;
            }
            return descriptionsForType[type];
        }

        public static IDictionary<string, IList<ComponentDescription>> Descriptions => new ReadOnlyDictionary<string, IList<ComponentDescription>>(descriptionsForType);
    }
}
