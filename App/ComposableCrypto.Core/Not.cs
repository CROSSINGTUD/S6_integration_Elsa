namespace ComposableCrypto.Core
{
    public class Not : Condition
    {
        public Condition Condition { get; }

        public Not(Condition condition)
        {
            Condition = condition;
        }

        public override bool Evaluate(Component component)
        {
            return !Condition.Evaluate(component);
        }

        public override string ToString()
        {
            return $"¬({Condition})";
        }

        public override string ToHighlightedString(Component component)
        {
            return $"¬({Condition.ToHighlightedString(component)})";
        }
    }
}
