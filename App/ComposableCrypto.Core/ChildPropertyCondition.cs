namespace ComposableCrypto.Core
{
    public class ChildPropertyCondition : Condition
    {
        public string ChildName { get; }
        public string PropertyName { get; }

        public ChildPropertyCondition(string childName, string propertyName)
        {
            ChildName = childName;
            PropertyName = propertyName;
        }

        public override bool Evaluate(Component component)
        {
            if (component.Children.TryGetValue(ChildName, out Component child))
            {
                return child.Description.GetPropertyOrFalse(PropertyName).Evaluate(child);
            }
            else
            {
                return false;
            }
        }

        public override string ToString()
        {
            return $"{ChildName}.{PropertyName}";
        }

        public override string ToHighlightedString(Component component)
        {
            if (Evaluate(component))
            {
                return $"<span class=\"bg-success text-white\">{ToString()}</span>";
            }
            else
            {
                return $"<span class=\"bg-danger text-white\">{ToString()}</span>";
            }
        }
    }
}
