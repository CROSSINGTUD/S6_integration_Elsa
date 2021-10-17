using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ComposableCrypto.CrySLGen
{
    public class TypeRegistry
    {
        private static readonly IDictionary<string, string> interfaces = new Dictionary<string, string>();
        private static readonly IDictionary<string, string> classes = new Dictionary<string, string>();

        public static void RegisterInterface(string componentType, string fullyQualifiedInterfaceName)
        {
            _ = interfaces.TryAdd(componentType, fullyQualifiedInterfaceName);
        }

        public static void RegisterComponentClass(string componentName, string fullyQualifiedClassName)
        {
            classes.Add(componentName, fullyQualifiedClassName);
        }

        internal static string InterfaceNameForComponentType(string componentType)
        {
            return interfaces[componentType];
        }

        internal static  string ClassNameForComponent(string componentName)
        {
            return classes[componentName];
        }
    }
}
