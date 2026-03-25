package cn.ayeez.blogserver.service.postServer;

import cn.ayeez.blogcommon.util.Result;
import cn.ayeez.blogpojo.dto.request.LogVersionBody;
import cn.ayeez.blogpojo.dto.response.LogVersionDetail;

import java.util.List;

/**
 * 站点日志服务
 */
public interface LogService {

    /**
     * 获取日志版本列表（包含当前标记）
     */
    List<LogVersionDetail> list(String keyword);

    /**
     * 获取当前生效版本
     */
    LogVersionDetail current();

    /**
     * 新增日志版本
     */
    Result add(LogVersionBody body);

    /**
     * 编辑日志版本
     */
    Result update(LogVersionBody body);

    /**
     * 删除日志版本
     */
    Result delete(Long id);

    /**
     * 设置某个版本为当前生效版本
     */
    Result setCurrent(Long id);
}

