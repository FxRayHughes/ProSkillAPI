import { ActionIcon, Tooltip } from '@mantine/core';
import type { ReactNode } from 'react';

/** Icon-only commands share accessible names and keyboard-focusable Mantine controls. */
export function ToolButton({
  label,
  onClick,
  children,
}: {
  label: string;
  onClick: () => void;
  children: ReactNode;
}) {
  return (
    <Tooltip label={label}>
      <ActionIcon aria-label={label} variant="default" size="lg" onClick={onClick}>
        {children}
      </ActionIcon>
    </Tooltip>
  );
}
