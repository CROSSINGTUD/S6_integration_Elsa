using System.Collections.Generic;
using System.Text;

namespace ComposableCrypto.CrySLGen
{
    internal class CrySLRule
    {
        internal string TypeName { get; }
        internal string FileName => JavaPackageUtils.ClassOrInterfaceName(TypeName) + ".crysl";
        internal IList<string> Events { get; } = new List<string>();
        internal StringBuilder ObjectsSection { get; } = new();
        internal StringBuilder EventsSection { get; } = new();
        internal StringBuilder RequiresSection { get; } = new();
        internal StringBuilder EnsuresSection { get; } = new();

        public CrySLRule(string typeName)
        {
            TypeName = typeName;
        }

        public override string ToString()
        {
            StringBuilder sb = new();
            sb.Append("SPEC ");
            sb.AppendLine(TypeName);
            sb.AppendLine();
            sb.AppendLine("OBJECTS");
            sb.Append(ObjectsSection);
            sb.AppendLine();
            sb.AppendLine();
            sb.AppendLine("EVENTS");
            sb.Append(EventsSection);
            sb.AppendLine();
            sb.AppendLine();
            sb.AppendLine("ORDER");
            sb.Append('\t');
            sb.AppendJoin(" | ", Events);
            sb.AppendLine();
            sb.AppendLine();
            if (RequiresSection.Length != 0)
            {
                sb.AppendLine("REQUIRES");
                sb.Append(RequiresSection);
                sb.AppendLine();
                sb.AppendLine();
            }
            if (EnsuresSection.Length != 0)
            {
                sb.AppendLine("ENSURES");
                sb.Append(EnsuresSection);
                sb.AppendLine();
                sb.AppendLine();
            }
            return sb.ToString();
        }
    }
}