export function Pagination({
  page,
  total,
  onChange,
}: {
  page: number;
  total: number;
  onChange: (page: number) => void;
}) {
  return (
    <nav aria-label="Pagination" className="pagination">
      <button disabled={page === 0} onClick={() => onChange(page - 1)}>
        Previous
      </button>
      <span>
        Page {page + 1} of {Math.max(total, 1)}
      </span>
      <button disabled={page + 1 >= total} onClick={() => onChange(page + 1)}>
        Next
      </button>
    </nav>
  );
}
