import type { SkillProject } from '../../skill-editor/model/types';

/** A skill file in the plugin directory; `error` explains why `project` is missing. */
export interface WorkspaceFile {
  name: string;
  fileName: string;
  handle: FileSystemFileHandle;
  project?: SkillProject;
  error?: string;
}
