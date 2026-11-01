package ${options.packageName}.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import ${options.packageName}.entity.${options.className};

/**
 * ${options.functionName} Mapper
 *
 * <p>確保應用的 {@code @MapperScan} 掃描到本包：${options.packageName}.mapper
 *
 * @author ${options.author}
 */
public interface ${options.className}Mapper extends BaseMapper<${options.className}> {
}
