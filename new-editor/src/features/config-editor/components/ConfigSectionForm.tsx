import { Accordion, Alert, Stack, Text } from '@mantine/core';
import { ConfigField } from './ConfigField';
import type { ConfigField as Field, ConfigSection } from '../model/configDocument';

/** Nested sections mirror the file, so settings the plugin adds later appear on their own. */
export function ConfigSectionForm({
  section,
  onChange,
  depth = 0,
}: {
  section: ConfigSection;
  onChange: (field: Field, value: boolean | number | string | string[]) => void;
  depth?: number;
}) {
  const empty = !section.fields.length && !section.sections.length;
  return (
    <Stack gap="sm">
      {section.description && (
        <Alert variant="light" color="gray" p="xs">
          <Text size="xs" style={{ whiteSpace: 'pre-wrap' }}>
            {section.description}
          </Text>
        </Alert>
      )}
      {section.fields.map((field) => (
        <ConfigField
          key={field.path.join('.')}
          field={field}
          onChange={(value) => onChange(field, value)}
        />
      ))}
      {section.sections.length > 0 && (
        // Top-level groups start open; deeper ones stay collapsed to keep long files scannable.
        <Accordion
          multiple
          variant="separated"
          defaultValue={depth === 0 ? section.sections.map((child) => child.key) : []}
        >
          {section.sections.map((child) => (
            <Accordion.Item key={child.path.join('.')} value={child.key}>
              <Accordion.Control>
                {child.key}
                <Text span size="xs" c="dimmed" ml="xs">
                  {child.fields.length + child.sections.length}
                </Text>
              </Accordion.Control>
              <Accordion.Panel>
                <ConfigSectionForm section={child} onChange={onChange} depth={depth + 1} />
              </Accordion.Panel>
            </Accordion.Item>
          ))}
        </Accordion>
      )}
      {empty && (
        <Text size="sm" c="dimmed">
          该段没有可视化编辑的项，保存时原样保留。
        </Text>
      )}
    </Stack>
  );
}
