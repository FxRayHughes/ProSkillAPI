import { createTheme } from '@mantine/core';

/** Keep toolkit dimensions shared; graph nodes use category colors independently. */
export const theme = createTheme({
  primaryColor: 'teal',
  defaultRadius: 'sm',
  fontFamily: '"Segoe UI", "Microsoft YaHei", sans-serif',
  headings: { fontFamily: '"Segoe UI", "Microsoft YaHei", sans-serif' },
});
