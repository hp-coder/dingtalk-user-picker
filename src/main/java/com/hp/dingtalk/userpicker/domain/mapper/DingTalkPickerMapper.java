package com.hp.dingtalk.userpicker.domain.mapper;

import com.hp.common.base.mapper.DateMapper;
import com.hp.common.base.mapper.GenericEnumMapper;
import com.hp.dingtalk.userpicker.domain.DingTalkPickerNode;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author hp
 */
@Mapper(
        uses = {
                GenericEnumMapper.class,
                DateMapper.class,
        }
)
public interface DingTalkPickerMapper {
    DingTalkPickerMapper INSTANCE = Mappers.getMapper(DingTalkPickerMapper.class);

    DingTalkPickerNode copy(DingTalkPickerNode node);
}
