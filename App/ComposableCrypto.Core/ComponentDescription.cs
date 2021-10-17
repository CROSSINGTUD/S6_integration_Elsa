using System.Collections.Generic;

namespace ComposableCrypto.Core
{
    public class ComponentDescription
    {
        public string Type { get; internal set; }
        public string Name { get; internal set; }
        public string DisplayName { get; internal set; }
        public IDictionary<string, Condition> Properties { get; internal set; }
        public IDictionary<string, ChildDescription> Children { get; internal set; }

        internal ComponentDescription()
        {

        }

        public static ComponentDescriptionBuilder Builder()
        {
            return new ComponentDescriptionBuilder();
        }

        public Condition GetPropertyOrFalse(string conditionName)
        {
            if (Properties.TryGetValue(conditionName, out Condition condition))
            {
                return condition;
            }

            return Condition.FALSE;
        }
    }
}
