import {
  ActionIcon,
  Group,
  NumberInput,
  Stack,
  Switch,
  Text,
  Textarea,
  TextInput,
} from '@mantine/core';
import { Plus, Trash2 } from 'lucide-react';
import { useState } from 'react';
import type { SkillMeta } from '../model/types';

/** Attribute rows the legacy editor always writes; custom keys stay editable alongside them. */
const KNOWN_ATTRIBUTES = [
  { key: 'level', label: '领悟等级' },
  { key: 'cost', label: '技能点消耗' },
  { key: 'cooldown', label: '冷却时间' },
  { key: 'mana', label: '法力消耗' },
  { key: 'points-spent-req', label: '已用技能点需求' },
];

/** Only real -base/-scale pairs become rows; other settings keep their own controls. */
function attributeNames(attributes: SkillMeta['attributes']): string[] {
  const names = new Set(KNOWN_ATTRIBUTES.map((entry) => entry.key));
  for (const key of Object.keys(attributes)) {
    const match = /^(.*)-(base|scale)$/.exec(key);
    if (match) names.add(match[1]);
  }
  return [...names];
}

function labelFor(name: string): string {
  return KNOWN_ATTRIBUTES.find((entry) => entry.key === name)?.label ?? name;
}

/** Settings.load reads every key, so list settings are editable instead of pass-through. */
function stringList(value: string | string[] | undefined): string[] {
  return Array.isArray(value) ? value : [];
}

/** Mirrors the native skill section so nothing the server reads is missing from the form. */
export function SkillMetadata({
  meta,
  onChange,
}: {
  meta: SkillMeta;
  onChange: (meta: Partial<SkillMeta>) => void;
}) {
  const [newAttribute, setNewAttribute] = useState('');
  const setAttribute = (key: string, value: string) =>
    onChange({ attributes: { ...meta.attributes, [key]: value } });
  const removeAttribute = (name: string) =>
    onChange({
      attributes: Object.fromEntries(
        Object.entries(meta.attributes).filter(
          ([key]) => key !== `${name}-base` && key !== `${name}-scale`,
        ),
      ),
    });
  return (
    <Stack gap="sm">
      <Text fw={600} size="sm">
        技能属性
      </Text>
      <TextInput
        label="技能名称"
        description="同时是文件名与其他配置引用该技能的键"
        value={meta.name}
        onChange={(event) => onChange({ name: event.currentTarget.value })}
      />
      <TextInput
        label="技能类型"
        description="显示用分类，例如“主动技能”“被动技能”"
        value={meta.type}
        onChange={(event) => onChange({ type: event.currentTarget.value })}
      />
      <NumberInput
        label="最大等级"
        min={1}
        allowDecimal={false}
        value={meta.maxLevel}
        onChange={(value) => {
          if (typeof value === 'number' && Number.isSafeInteger(value) && value >= 1)
            onChange({ maxLevel: value });
        }}
      />
      <TextInput
        label="前置技能"
        description="留空表示没有前置技能"
        value={meta.skillReq}
        onChange={(event) => onChange({ skillReq: event.currentTarget.value })}
      />
      <NumberInput
        label="前置技能等级"
        min={0}
        allowDecimal={false}
        value={meta.skillReqLevel}
        onChange={(value) => {
          if (typeof value === 'number' && Number.isSafeInteger(value) && value >= 0)
            onChange({ skillReqLevel: value });
        }}
      />
      <Switch
        label="需要权限才能学习"
        checked={meta.needsPermission}
        onChange={(event) => onChange({ needsPermission: event.currentTarget.checked })}
      />
      <TextInput
        label="施放提示消息"
        description="支持 {player} {skill} 等占位符"
        value={meta.msg}
        onChange={(event) => onChange({ msg: event.currentTarget.value })}
      />
      <TextInput
        label="连招"
        description="留空表示不使用连招施放"
        value={meta.combo}
        onChange={(event) => onChange({ combo: event.currentTarget.value })}
      />

      <Text fw={600} size="sm" mt="xs">
        技能数值
      </Text>
      <Text size="xs" c="dimmed">
        基础值与每级成长都可以填写数值或服务端支持的公式。
      </Text>
      {attributeNames(meta.attributes).map((name) => (
        <Group key={name} align="flex-end" wrap="nowrap" gap="xs">
          <TextInput
            label={`${labelFor(name)} · 基础值`}
            description={`${name}-base`}
            value={String(meta.attributes[`${name}-base`] ?? '')}
            onChange={(event) => setAttribute(`${name}-base`, event.currentTarget.value)}
            style={{ flex: 1 }}
          />
          <TextInput
            label="每级成长"
            description={`${name}-scale`}
            value={String(meta.attributes[`${name}-scale`] ?? '')}
            onChange={(event) => setAttribute(`${name}-scale`, event.currentTarget.value)}
            style={{ flex: 1 }}
          />
          <ActionIcon
            variant="subtle"
            color="red"
            aria-label={`删除 ${labelFor(name)}`}
            onClick={() => removeAttribute(name)}
          >
            <Trash2 size={16} />
          </ActionIcon>
        </Group>
      ))}
      <Group align="flex-end" wrap="nowrap" gap="xs">
        <TextInput
          label="新增数值"
          placeholder="英文键，例如 damage"
          value={newAttribute}
          onChange={(event) => setNewAttribute(event.currentTarget.value)}
          style={{ flex: 1 }}
        />
        <ActionIcon
          variant="light"
          aria-label="新增技能数值"
          disabled={!newAttribute.trim()}
          onClick={() => {
            const name = newAttribute.trim();
            onChange({
              attributes: { ...meta.attributes, [`${name}-base`]: '0', [`${name}-scale`]: '0' },
            });
            setNewAttribute('');
          }}
        >
          <Plus size={16} />
        </ActionIcon>
      </Group>
      <Textarea
        label="互斥技能"
        description="每行一个技能名；已学习其中任意一个时无法学习本技能"
        autosize
        minRows={1}
        value={stringList(meta.attributes.incompatible).join('\n')}
        onChange={(event) =>
          onChange({
            attributes: {
              ...meta.attributes,
              incompatible:
                event.currentTarget.value === '' ? [] : event.currentTarget.value.split('\n'),
            },
          })
        }
      />

      <Text fw={600} size="sm" mt="xs">
        图标与描述
      </Text>
      <TextInput
        label="图标材质 ID"
        value={meta.icon}
        onChange={(event) => onChange({ icon: event.currentTarget.value })}
      />
      <Group grow>
        <NumberInput
          label="图标数据值"
          min={0}
          allowDecimal={false}
          value={meta.iconData}
          onChange={(value) => typeof value === 'number' && onChange({ iconData: value })}
        />
        <NumberInput
          label="图标耐久值"
          min={0}
          allowDecimal={false}
          value={meta.iconDurability}
          onChange={(value) => typeof value === 'number' && onChange({ iconDurability: value })}
        />
      </Group>
      <Textarea
        label="图标 Lore"
        description="每行一条，支持 &amp; 颜色码与 {attr:...} 占位符"
        autosize
        minRows={3}
        value={meta.iconLore.join('\n')}
        onChange={(event) =>
          onChange({
            iconLore: event.currentTarget.value === '' ? [] : event.currentTarget.value.split('\n'),
          })
        }
      />
      <Textarea
        label="技能描述"
        description="每行一条"
        autosize
        minRows={2}
        value={meta.description.join('\n')}
        onChange={(event) =>
          onChange({
            description:
              event.currentTarget.value === '' ? [] : event.currentTarget.value.split('\n'),
          })
        }
      />
    </Stack>
  );
}
