package com.jopsonframework.starter.core.web;

import com.jopsonframework.starter.core.enums.ValueEnum;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/** MVC 的属性绑定会在 ConversionFailedException 后用静态字段名重试。业务编码的失败要直接交给绑定错误处理。 */
public final class ValueEnumBindingPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (bean instanceof RequestMappingHandlerAdapter adapter
                && adapter.getWebBindingInitializer() instanceof ConfigurableWebBindingInitializer initializer
                && initializer.getConversionService() != null) {
            initializer.setConversionService(new BindingConversions(initializer.getConversionService()));
        }
        return bean;
    }

    private record BindingConversions(ConversionService delegate) implements ConversionService {
        @Override
        public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
            return delegate.canConvert(sourceType, targetType);
        }

        @Override
        public boolean canConvert(TypeDescriptor sourceType, TypeDescriptor targetType) {
            return delegate.canConvert(sourceType, targetType);
        }

        @Override
        public <T> T convert(Object source, Class<T> targetType) {
            return delegate.convert(source, targetType);
        }

        @Override
        public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
            try {
                return delegate.convert(source, sourceType, targetType);
            } catch (ConversionFailedException ex) {
                if (ValueEnum.class.isAssignableFrom(ex.getTargetType().getType())) {
                    throw new IllegalArgumentException("未知的业务枚举编码", ex);
                }
                throw ex;
            }
        }
    }
}
