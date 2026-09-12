import { Select, Tooltip } from '@mantine/core';
import { SERVER_VERSIONS } from '../lib/preferences';

interface ServerVersionSelectProps {
  value: string;
  onChange: (version: string) => void;
}

/**
 * 全局的目标服务端版本选择器。
 *
 * 该版本只决定枚举选项（粒子、材质、音效）如何按可用区间过滤，不写入技能文件，
 * 所以它是整个编辑器的一个设置，而不是逐技能保存的属性。
 */
export function ServerVersionSelect({ value, onChange }: ServerVersionSelectProps) {
  return (
    <Tooltip label="决定粒子、材质等枚举按哪个版本过滤；不写入技能文件" position="bottom" withArrow>
      <Select
        aria-label="目标服务端版本"
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
