using ComposableCrypto.Core;
using System.Collections.Generic;
using System.Text;

namespace ComposableCrypto.CrySLGen
{
    public class CrySLTaskGenerator
    {
        private readonly HashSet<ComponentDescription> generatedComponents = new();
        private readonly StringBuilder sb = new();
        private readonly Component rootComponent;

        public CrySLTaskGenerator(Component rootComponent)
        {
            this.rootComponent = rootComponent;
        }

        public string GenerateCrySLTask()
        {
            sb.AppendFormat("{0} cryptographicComponents = null;", TypeRegistry.ClassNameForComponent(rootComponent.Description.Name));
            sb.AppendLine();
            sb.AppendFormat("CrySLCodeGenerator.getInstance()");
            Generate(rootComponent);
            sb.Append(';');
            return sb.ToString();
        }

        private void Generate(Component component)
        {
            foreach (Component child in component.Children.Values)
            {
                Generate(child);
            }
            if (generatedComponents.Add(component.Description))
            {
                string className = TypeRegistry.ClassNameForComponent(component.Description.Name);
                sb.AppendLine();
                sb.AppendFormat("\t.includeClass(\"{0}\")", className);
                if (component == rootComponent)
                {
                    sb.AppendLine();
                    sb.Append("\t.addParameter(cryptographicComponents, \"obj\")");
                }
            }
        }
    }
}
