import test from 'node:test';
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
const text = await readFile(new URL('../../main/resources/static/timeline.js', import.meta.url), 'utf8');
const { createState, applyEvent, rowsFor } = await import(`data:text/javascript;base64,${Buffer.from(text).toString('base64')}`);
test('native lifecycle and recipe events sort by capture sequence within one producer', () => {
    const state = createState();
    applyEvent(state, { seq:1, type:'run-start', runId:'r', producerId:'p', sourceSeq:6, reannounce:true });
    applyEvent(state, { seq:2, type:'recipe-event', runId:'r', producerId:'p', sourceSeq:5 });
    const result = rowsFor(state, 'r');
    assert.equal(result.capture, true);
    assert.deepEqual(result.rows.map(event => event.sourceSeq), [5,6]);
});
test('reused run id across producer instances falls back to server order', () => {
    const state = createState();
    applyEvent(state, { seq:1, type:'run-start', runId:'r', producerId:'p1', sourceSeq:9 });
    applyEvent(state, { seq:2, type:'run-start', runId:'r', producerId:'p2', sourceSeq:1 });
    assert.equal(rowsFor(state, 'r').capture, false);
    assert.deepEqual(rowsFor(state, 'r').rows.map(event => event.seq), [1,2]);
});