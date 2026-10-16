import test from 'node:test';
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
const text = await readFile(new URL('../../main/resources/static/timeline.js', import.meta.url), 'utf8');
const { createState, applyEvent, rowsFor, UI_LIMIT } = await import(`data:text/javascript;base64,${Buffer.from(text).toString('base64')}`);

test('legacy capture order differs from server arrival and reconnect copies deduplicate by seq', () => {
    const state = createState();
    const event = { type:'recipe-event', runId:'p', seq:1, sourceSeq:2 };
    applyEvent(state, event); applyEvent(state, event);
    applyEvent(state, { type:'recipe-event', runId:'p', seq:2, sourceSeq:1 });
    assert.equal(state.events.size, 2);
    assert.equal(rowsFor(state, 'p').capture, true);
    assert.deepEqual(rowsFor(state, 'p').rows.map(row => row.sourceSeq), [1,2]);
});
test('native events retain server order rather than inventing capture order', () => {
    const state = createState();
    applyEvent(state, { type:'client-response', runId:'r', seq:2, ts:1 });
    applyEvent(state, { type:'client-request', runId:'r', seq:1, ts:99 });
    const result = rowsFor(state, 'r');
    assert.equal(result.capture, false);
    assert.deepEqual(result.rows.map(row => row.seq), [1,2]);
});
test('clear resets view; a new connection uses new state when server seq restarts', () => {
    let state = createState();
    applyEvent(state, { type:'run-start', runId:'old', seq:200 });
    applyEvent(state, { type:'clear' });
    assert.equal(state.events.size, 0);
    applyEvent(state, { type:'run-start', runId:'current', seq:201 });
    state = createState();
    applyEvent(state, { type:'run-start', runId:'new', seq:1 });
    assert.deepEqual([...state.events.keys()], [1]);
});
test('UI has a global event cap and reports discarded rows', () => {
    const state = createState();
    for (let seq=1; seq<=UI_LIMIT+2; seq++) applyEvent(state, { type:'run-start', runId:'r', seq });
    assert.equal(state.events.size, UI_LIMIT);
    assert.equal(state.discarded, 2);
    assert.equal(state.events.has(1), false);
});