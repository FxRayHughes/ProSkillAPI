/** Native plugin layout read by RegistrationManager: dynamic/skill and dynamic/class. */
export const SKILL_DIR = ['dynamic', 'skill'] as const;
export const CLASS_DIR = ['dynamic', 'class'] as const;

export interface PluginRoot {
  handle: FileSystemDirectoryHandle;
  skills: FileSystemDirectoryHandle;
  classes: FileSystemDirectoryHandle;
}

export interface DirectoryEntry {
  /** File name without the .yml extension; it is also the YAML section key. */
  name: string;
  fileName: string;
  handle: FileSystemFileHandle;
  text: string;
}

async function descend(
  root: FileSystemDirectoryHandle,
  path: readonly string[],
  create: boolean,
): Promise<FileSystemDirectoryHandle> {
  let current = root;
  for (const segment of path) current = await current.getDirectoryHandle(segment, { create });
  return current;
}

/**
 * Resolves the two directories the server loads from. Missing directories are reported with
 * the expected layout so the user can tell a wrong folder from an empty one.
 */
export async function resolvePluginRoot(handle: FileSystemDirectoryHandle): Promise<PluginRoot> {
  const open = async (path: readonly string[]) => {
    try {
      return await descend(handle, path, false);
    } catch {
      throw new Error(
        `所选文件夹不是 SkillAPI 插件目录：未找到 ${path.join('/')}。请选择包含 dynamic 文件夹的插件目录。`,
      );
    }
  };
  return { handle, skills: await open(SKILL_DIR), classes: await open(CLASS_DIR) };
}

/** Reads raw text so each caller decides how to parse and how to report a bad file. */
export async function listYamlFiles(
  directory: FileSystemDirectoryHandle,
): Promise<DirectoryEntry[]> {
  const entries: DirectoryEntry[] = [];
  for await (const [fileName, entry] of directory.entries()) {
    if (entry.kind !== 'file' || !/\.ya?ml$/i.test(fileName)) continue;
    const handle = entry as FileSystemFileHandle;
    entries.push({
      name: fileName.replace(/\.ya?ml$/i, ''),
      fileName,
      handle,
      text: await handle.getFile().then((file) => file.text()),
    });
  }
  return entries.sort((a, b) => a.name.localeCompare(b.name, 'zh-Hans-CN'));
}

export async function writeFile(handle: FileSystemFileHandle, content: string): Promise<void> {
  const writable = await handle.createWritable();
  await writable.write(content);
  await writable.close();
}

/** File name must equal the section key; the server derives the skill name from it. */
export async function createYamlFile(
  directory: FileSystemDirectoryHandle,
  name: string,
  content: string,
): Promise<FileSystemFileHandle> {
  const handle = await directory.getFileHandle(`${name}.yml`, { create: true });
  await writeFile(handle, content);
  return handle;
}

export async function fileExists(
  directory: FileSystemDirectoryHandle,
  name: string,
): Promise<boolean> {
  try {
    await directory.getFileHandle(`${name}.yml`);
    return true;
  } catch {
    return false;
  }
}
