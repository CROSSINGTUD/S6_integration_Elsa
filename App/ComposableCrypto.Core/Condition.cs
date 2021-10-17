namespace ComposableCrypto.Core
{
    public abstract class Condition
    {
        public abstract bool Evaluate(Component component);
        public abstract string ToHighlightedString(Component component);
        public static readonly Condition TRUE = new StaticCondition(true);
        public static readonly Condition FALSE = new StaticCondition(false);

        private class StaticCondition : Condition
        {
            private readonly bool value;
            
            public StaticCondition(bool value)
            {
                this.value = value;
            }

            public override bool Evaluate(Component component)
            {
                return value;
            }

            public override string ToHighlightedString(Component component)
            {
                if (value)
                {
                    return $"<span class=\"bg-success text-white\">{value}</span>";
                }
                else
                {
                    return $"<span class=\"bg-danger text-white\">{value}</span>";
                }
            }

            public override string ToString()
            {
                return value.ToString();
            }
        }
    }
}
