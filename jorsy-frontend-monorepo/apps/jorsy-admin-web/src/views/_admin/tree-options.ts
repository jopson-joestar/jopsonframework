export interface TreeOption {
  label: string;
  value: string;
  children?: TreeOption[];
}

export function treeOptions<R extends { children: R[] }>(
  rows: R[],
  value: (row: R) => string,
  label: (row: R) => string,
  exclude?: string,
): TreeOption[] {
  return rows
    .filter((row) => value(row) !== exclude)
    .map((row) => ({
      label: label(row),
      value: value(row),
      children: treeOptions(row.children, value, label, exclude),
    }));
}
