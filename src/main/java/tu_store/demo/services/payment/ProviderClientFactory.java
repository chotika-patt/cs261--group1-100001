package tu_store.demo.services.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ProviderClientFactory {
    @Autowired
    private ApplicationContext ctx;

    public ProviderClient getClient(String provider) {
        if (provider == null) provider = "MOCK";
        provider = provider.trim();
        if (ctx.containsBean(provider)) {
            return ctx.getBean(provider, ProviderClient.class);
        }

        //fallback
        return ctx.getBean("MOCK", ProviderClient.class);
    }
}
