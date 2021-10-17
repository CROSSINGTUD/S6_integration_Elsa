namespace ComposableCrypto.CrySLGen
{
    internal static class JavaPackageUtils
    {
        internal static string ClassOrInterfaceName(string fullyQualifiedName)
        {
            return fullyQualifiedName.Substring(fullyQualifiedName.LastIndexOf('.') + 1);
        }
    }
}
