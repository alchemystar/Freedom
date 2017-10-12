package alchemystar.freedom.store.page;

import alchemystar.freedom.config.SystemConfig;
import alchemystar.freedom.index.bp.BPNode;
import alchemystar.freedom.index.bp.BpPage;

/**
 * BPFactory
 *
 * @Author lizhuyang
 */
public class PageFactory {

    private static PageFactory factory = new PageFactory();

    public static PageFactory getInstance() {
        return factory;
    }

    private PageFactory() {
    }

    public Page newPage() {
        return new Page(SystemConfig.DEFAULT_PAGE_SIZE);
    }

    public BpPage newBpPage(BPNode bpNode) {
        return new BpPage(bpNode);
    }

}
