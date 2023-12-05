package com.luban.dingtalk.userpicker.infrastructure.repository.cache;

import com.google.common.base.Preconditions;
import com.luban.common.base.annotations.FieldDesc;
import com.luban.dingtalk.userpicker.domain.DingTalkPickerNode;
import com.luban.dingtalk.userpicker.domain.DingTalkPickerNodeSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

/**
 * @author hp
 */
@Slf4j
@RequiredArgsConstructor
public class LocalFileBasedDingTalkPickerCache implements IDingTalkUserPickerCache {

    @FieldDesc("本地文件副本")
    private static final String LOCAL_FILE_FORMAT = "dingtalk_picker_%s.json";

    @Override
    public int priority() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public boolean save(DingTalkPickerNodeSource source, DingTalkPickerNode root) {
        Preconditions.checkArgument(Objects.nonNull(root));
        try {
            writeToFile(source, root);
            return true;
        } catch (IOException e) {
            log.error("Write DingTalk organization architecture to the {} failed.", getLocalFileName(source), e);
            return false;
        }
    }

    @Override
    public Optional<DingTalkPickerNode> findAll(DingTalkPickerNodeSource source) {
        if (exist(source)) {
            try {
                return Optional.ofNullable(readFromFile(source));
            } catch (IOException e) {
                log.error("Read DingTalk organization architecture from the {} failed", getLocalFileName(source), e);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean exist(DingTalkPickerNodeSource source) {
        final File file = new File(getLocalFileName(source));
        return (file.exists() && file.length() > 0);
    }

    private String getLocalFileName(DingTalkPickerNodeSource source) {
        return String.format(LOCAL_FILE_FORMAT, source.getCode());
    }

    private DingTalkPickerNode readFromFile(DingTalkPickerNodeSource source) throws IOException {
        final String json = new String(Files.readAllBytes(Paths.get(getLocalFileName(source))));
        return DingTalkPickerNode.deserialize(json);
    }

    private void writeToFile(DingTalkPickerNodeSource source, DingTalkPickerNode root) throws IOException {
        FileWriter writer = new FileWriter(getLocalFileName(source));
        writer.write(DingTalkPickerNode.serialize(root));
        writer.close();
        log.info("Successfully write the DingTalk arch to the file");
    }
}
