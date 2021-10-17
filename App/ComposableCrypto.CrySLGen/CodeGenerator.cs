using ComposableCrypto.Core;
using System.Linq;
using System.Text;

namespace ComposableCrypto.CrySLGen
{
    public class CodeGenerator
    {
        private readonly Component rootComponent;
        private readonly StringBuilder sb = new();

        public CodeGenerator(Component rootComponent)
        {
            this.rootComponent = rootComponent;
        }

        public string Generate()
        {
            sb.AppendLine();
            sb.Append("\t\t");
            sb.Append(TypeRegistry.ClassNameForComponent(rootComponent.Description.Name) + ".Builder");
            sb.AppendLine(" construction = ");
            Generate(rootComponent, 3);
            sb.AppendLine(";");
            return sb.ToString();
        }

        private void Generate(Component component, int indentationLevel = 1)
        {
            for (int i = 0; i < indentationLevel; i++)
            {
                sb.Append('\t');
            }
            sb.Append(TypeRegistry.ClassNameForComponent(component.Description.Name));
            sb.Append(".builder(");
            Component lastChild = component.Children.Values.LastOrDefault();
            foreach (Component child in component.Children.Values)
            {
                sb.AppendLine();
                Generate(child, indentationLevel + 1);
                if (child == lastChild)
                {
                    sb.AppendLine();
                }
                else
                {
                    sb.Append(',');
                }
            }
            if (component.Children.Any())
            {
                for (int i = 0; i < indentationLevel; i++)
                {
                    sb.Append('\t');
                }
            }
            sb.Append(')');
        }
    }
}
