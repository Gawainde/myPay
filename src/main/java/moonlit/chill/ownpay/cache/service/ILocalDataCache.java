package moonlit.chill.ownpay.cache.service;

/**
 * @author Gawaind
 * @date 2024/11/7 10:52
 */
public interface ILocalDataCache {

    void refresh();

    void initData();

    default int getOrder() {
        return 100;
    }
}
