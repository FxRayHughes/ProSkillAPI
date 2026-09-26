import { Select, Tooltip } from '@mantine/core';
import { SERVER_VERSIONS } from '../lib/preferences';

interface ServerVersionSelectProps {
  value: string;
  onChange: (version: string) => void;
}

/**
 * 主页上的全局目标服务端版本选择器。
 *
 * 该版本只决定枚举预览，不写入技能文件；粒子 AST 在服务端按实际版本解析。
 */
export function ServerVersionSelect({ value, onChange }: ServerVersionSelectProps) {
  return (
    <Tooltip
      label="预览粒子等枚举在指定版本的可用情况；不影响导出的 AST"
      position="bottom"
      withArrow
    >
      <Select
        aria-label="预览服务端版本"
        size="xs"
        w={112}
        allowDeselect={false}
        searchable
        comboboxProps={{ withinPortal: true }}
        data={SERVER_VERSIONS.map((version) => ({ value: version, label: version }))}
        value={value}
        onChange={(next) => next && onChange(next)}
      />
    </Tooltip>
  );
}
