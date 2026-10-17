export const UI_LIMIT = 2000;
export function createState() { return { events: new Map(), discarded: 0 }; }
export function applyEvent(state, event) {
    if (event.type === 'clear') { state.events.clear(); state.discarded = 0; return; }
    if (!Number.isSafeInteger(event.seq) || event.seq < 1) return;
    state.events.set(event.seq, event);
    while (state.events.size > UI_LIMIT) {
        state.events.delete(Math.min(...state.events.keys()));
        state.discarded++;
    }
}
export function runOf(event) { return String(event.runId ?? event.producerId ?? '(no run)'); }
export function rowsFor(state, run) {
    const rows = [...state.events.values()].filter(event => runOf(event) === run);
    const producers = new Set(rows.map(event => event.producerId));
    const capture = rows.length > 0 && rows.every(event =>
        Number.isSafeInteger(event.sourceSeq) && event.sourceSeq > 0) && (
        (producers.size === 1 && typeof rows[0].producerId === 'string' && rows[0].producerId.length > 0) ||
        rows.every(event => event.type === 'recipe-event' && event.producerId == null));
    rows.sort((a, b) => capture ? a.sourceSeq - b.sourceSeq || a.seq - b.seq : a.seq - b.seq);
    return { rows, capture };
}