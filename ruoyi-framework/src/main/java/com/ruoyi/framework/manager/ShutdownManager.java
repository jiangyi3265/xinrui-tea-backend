package com.ruoyi.framework.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.concurrent.ScheduledExecutorService;
import com.ruoyi.common.utils.Threads;

/**
 * 确保应用退出时能关闭后台线程
 *
 * @author ruoyi
 */
@Component
public class ShutdownManager
{
    private static final Logger logger = LoggerFactory.getLogger("sys-user");
    @Resource(name = "scheduledExecutorService")
    private ScheduledExecutorService executor;

    @PreDestroy
    public void destroy()
    {
        shutdownAsyncManager();
    }

    /**
     * 停止异步执行任务
     */
    private void shutdownAsyncManager()
    {
        try
        {
            logger.info("====关闭后台任务任务线程池====");
            Threads.shutdownAndAwaitTermination(executor);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
    }
}
