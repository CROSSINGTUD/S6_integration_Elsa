using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;

namespace ComposableCrypto.Core
{
    public class ComponentDescriptionBuilder
    {
        private readonly ComponentDescription description;
        private readonly IDictionary<string, Condition> properties = new Dictionary<string, Condition>();
        private readonly IDictionary<string, ChildDescription> children = new SortedDictionary<string, ChildDescription>();

        internal ComponentDescriptionBuilder()
        {
            description = new ComponentDescription();
        }

        public ComponentDescriptionBuilder Type(string type)
        {
            description.Type = type;
            return this;
        }

        public ComponentDescriptionBuilder Name(string name)
        {
            description.Name = name;
            return this;
        }

        public ComponentDescriptionBuilder DisplayName(string displayName)
        {
            description.DisplayName = displayName;
            return this;
        }

        public ComponentDescriptionBuilder WithChild(string name, string displayName, string type)
        {
            children.Add(name, new ChildDescription
            {
                Name = name,
                DisplayName = displayName,
                Type = type
            });
            return this;
        }

        public ComponentDescriptionBuilder WithProperty(string name, Condition condition)
        {
            properties.Add(name, condition);
            return this;
        }

        public ComponentDescription Build()
        {
            description.Properties = new ReadOnlyDictionary<string, Condition>(properties);
            description.Children = new ReadOnlyDictionary<string, ChildDescription>(children);

            if (string.IsNullOrWhiteSpace(description.Type))
            {
                throw new InvalidOperationException("No type set.");
            }
            if (string.IsNullOrWhiteSpace(description.Name))
            {
                throw new InvalidOperationException("No name set.");
            }
            if (string.IsNullOrWhiteSpace(description.DisplayName))
            {
                throw new InvalidOperationException("No display name set.");
            }

            return description;
        }
    }
}
