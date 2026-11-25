import { requestClient } from '#/api/request';

export interface VersionedRow {
  version: number;
}

export interface PageResult<R> {
  items: R[];
  total: number;
  currentPage: number;
  pageSize: number;
}

export interface PageQuery {
  currentPage: number;
  pageSize: number;
}

export type FormValues = Record<string, unknown>;
export type SyncOperation = 'ADD' | 'DELETE' | 'UPDATE';

export function postAdmin<T>(
  resource: string,
  action: string,
  body?: unknown,
  params?: FormValues,
) {
  return requestClient.post<T>(`/admin/${resource}/${action}`, body, {
    params,
  });
}

export function recordsApi<R extends VersionedRow>(
  resource: string,
  idField: keyof R & string,
) {
  function id(row: R) {
    return String(row[idField]);
  }

  return {
    id,
    idField,
    page: (query: PageQuery & FormValues) =>
      postAdmin<PageResult<R>>(resource, 'page', query),
    tree: () => postAdmin<R[]>(resource, 'tree'),
    detail: (row: R) =>
      postAdmin<R>(resource, 'get-by-id', undefined, { [idField]: id(row) }),
    create: (form: FormValues) => postAdmin<string>(resource, 'create', form),
    update: (row: R, form: FormValues) =>
      postAdmin<string>(resource, 'update-by-id', {
        ...form,
        [idField]: id(row),
        version: row.version,
      }),
    remove: (row: R) =>
      postAdmin<void>(resource, 'delete-by-id', undefined, {
        [idField]: id(row),
        version: row.version,
      }),
  };
}

export type RecordsApi<R extends VersionedRow> = ReturnType<
  typeof recordsApi<R>
>;
