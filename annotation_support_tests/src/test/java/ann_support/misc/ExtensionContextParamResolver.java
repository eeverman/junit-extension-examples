package ann_support.misc;

import org.junit.jupiter.api.extension.*;

public class ExtensionContextParamResolver implements ParameterResolver  {
	@Override
	public boolean supportsParameter(final ParameterContext parameterContext,
			final ExtensionContext extensionContext) throws ParameterResolutionException {
		return parameterContext.getParameter().getType() == ExtensionContext.class;
	}

	@Override
	public ExtensionContext resolveParameter(final ParameterContext parameterContext,
			final ExtensionContext extensionContext) throws ParameterResolutionException {
		return extensionContext;
	}
}
