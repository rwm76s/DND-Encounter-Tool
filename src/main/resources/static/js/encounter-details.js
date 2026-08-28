// ===== Turn Tracking =====
// currentTurnId tracks whose turn it is by combatant ID, not array/tile position.
// Position-based tracking would break the moment tiles get re-sorted by initiative.
let currentTurnId = initialTurnId;

// Always re-query the DOM rather than caching a NodeList - sortTilesByInitiative()
// physically reorders tiles in place, so a cached list would go stale.
function getTiles() {
    return Array.from(document.querySelectorAll('.combatant-tile'));
}

// Persists the current turn to the database so it survives page reloads
// and is consistent regardless of which device/browser the DM is using.
async function saveCurrentTurn(combatantId) {
    return apiFetch(`/api/encounters/${encounterId}/current-turn`, {
        method: 'PUT',
        body: JSON.stringify({ combatantId })
    });
}

function highlightCurrentTurn() {
    getTiles().forEach(tile => {
        tile.classList.toggle('current-turn', tile.dataset.combatantId === String(currentTurnId));
    });
}

// Steps one tile at a time in the given direction, skipping incapacitated combatants.
// Bounded by tiles.length attempts so it can't loop forever if everyone is down.
function findNextLivingIndex(tiles, startIndex, direction) {
    if (tiles.length === 0) return -1;

    let index = startIndex;
    for (let attempts = 0; attempts < tiles.length; attempts++) {
        index = ((index + direction) % tiles.length + tiles.length) % tiles.length;
        if (tiles[index].dataset.incapacitated !== 'true') {
            return index;
        }
    }
    return -1; // everyone is incapacitated so there is nothing to advance to
}

// direction is 1 for next, -1 for previous. Re-derives the current tile's position
// fresh each call, since DOM order can shift between calls (see sortTilesByInitiative).
async function goToTurn(direction) {
    const tiles = getTiles();
    if (tiles.length === 0) return;

    const startIndex = tiles.findIndex(tile => tile.dataset.combatantId === String(currentTurnId));
    const newIndex = findNextLivingIndex(tiles, startIndex === -1 ? -1 : startIndex, direction);
    if (newIndex === -1) return;

    const combatantId = tiles[newIndex].dataset.combatantId;

    await saveCurrentTurn(Number(combatantId));
    currentTurnId = Number(combatantId);
    highlightCurrentTurn();
}

function nextTurn() {
    goToTurn(1);
}

function previousTurn() {
    goToTurn(-1);
}

// Reorders tiles in the DOM to match current initiative values (highest first).
// Needed because AJAX updates no longer trigger a full page reload, so nothing
// else re-sorts the tiles after an initiative change.
function sortTilesByInitiative() {
    const grid = document.querySelector('.combatant-grid');
    const tileArray = getTiles();

    tileArray.sort((a, b) => {
        const initA = a.dataset.initiative === '' || a.dataset.initiative === undefined
            ? null : Number(a.dataset.initiative);
        const initB = b.dataset.initiative === '' || b.dataset.initiative === undefined
            ? null : Number(b.dataset.initiative);

        // combatants with no initiative set yet sink to the bottom, matching
        // Postgres's default "nulls last" behavior for ORDER BY ... DESC
        if (initA === null && initB === null) return 0;
        if (initA === null) return 1;
        if (initB === null) return -1;
        return initB - initA;
    });

    // appendChild on a node already in the DOM moves it rather than duplicating it,
    // so calling this in sorted order physically reorders the tiles.
    tileArray.forEach(tile => grid.appendChild(tile));
}

async function updateCombatant(combatantId, data) {
    return apiFetch(`/api/combatants/${combatantId}`, {
        method: 'PUT',
        body: JSON.stringify(data)
    });
}

// ===== Statuses =====

async function addStatusRequest(combatantId, statusText) {
    return apiFetch(`/api/combatants/${combatantId}/statuses`, {
        method: 'POST',
        body: JSON.stringify({ status: statusText })
    });
}

async function deleteStatusRequest(statusId) {
    return apiFetch(`/api/statuses/${statusId}`, { method: 'DELETE' });
}

// Shows/hides the "Statuses:" label based on whether the list currently has
// any items - keeps it in sync after add/delete without a page reload.
function refreshStatusLabel(tile) {
    const statusesDiv = tile.querySelector('.statuses');
    const list = statusesDiv.querySelector('.status-list');
    let label = statusesDiv.querySelector('.statuses-label');

    const hasStatuses = list.children.length > 0;

    if (hasStatuses && !label) {
        label = document.createElement('strong');
        label.className = 'statuses-label';
        label.textContent = 'Statuses:';
        statusesDiv.insertBefore(label, list);
    } else if (!hasStatuses && label) {
        label.remove();
    }
}

// Shared by both page-load statuses and newly-added ones so delete behavior
// stays identical regardless of how the status entered the page.
function attachDeleteHandler(button) {
    button.addEventListener('click', async () => {
        const li = button.closest('li');
        const tile = li.closest('.combatant-tile');
        const statusId = li.dataset.statusId;

        await deleteStatusRequest(statusId);
        li.remove();
        refreshStatusLabel(tile);
    });
}

// One shared modal for adding a status to any combatant, rather than one modal
// per tile. modalTargetTile remembers which tile triggered it.
const modal = document.getElementById('status-modal');
const modalInput = document.getElementById('status-modal-input');
const modalCancelBtn = document.getElementById('status-modal-cancel');
const modalSaveBtn = document.getElementById('status-modal-save');

let modalTargetTile = null;

function openStatusModal(tile) {
    modalTargetTile = tile;
    modalInput.value = '';
    modal.style.display = 'flex';
    modalInput.focus();
}

function closeStatusModal() {
    modal.style.display = 'none';
    modalTargetTile = null;
}

modalCancelBtn.addEventListener('click', closeStatusModal);

// Clicking the dark overlay (but not the box itself) closes the modal -
// event.target is only the overlay when the click didn't land on a child element.
modal.addEventListener('click', (event) => {
    if (event.target === modal) closeStatusModal();
});

modalSaveBtn.addEventListener('click', async () => {
    const statusText = modalInput.value.trim();
    if (!statusText || !modalTargetTile) return;

    const combatantId = modalTargetTile.dataset.combatantId;
    const newStatus = await addStatusRequest(combatantId, statusText);

    const list = modalTargetTile.querySelector('.status-list');
    const li = document.createElement('li');
    li.dataset.statusId = newStatus.id;
    li.innerHTML = `<span></span><button type="button" class="delete-status-btn">x</button>`;
    // set text via textContent (not innerHTML) so status text is never
    // interpreted as markup - protects against XSS from free-form input
    li.querySelector('span').textContent = newStatus.status;
    attachDeleteHandler(li.querySelector('.delete-status-btn'));
    list.appendChild(li);

    refreshStatusLabel(modalTargetTile);
    closeStatusModal();
});

// ===== Tile Initialization =====
// initTile wires up all interactive behavior for one tile (update, delete,
// statuses, notes). Called once per tile at page load, and again for any
// tile created dynamically after adding a new combatant.
function initTile(tile) {
    const updateBtn = tile.querySelector('.update-btn');
    if (updateBtn) {
        updateBtn.addEventListener('click', async () => {
            const combatantId = tile.dataset.combatantId;

            const initiativeInput = tile.querySelector('.initiative-input');
            const hpInput = tile.querySelector('.hp-input');
            const maxHpInput = tile.querySelector('.maxhp-input');
            const acInput = tile.querySelector('.ac-input');
            const incapacitatedInput = tile.querySelector('.incapacitated-input');

            // hp/maxHp/ac inputs don't exist on player tiles, so guard with ? :
            // empty string inputs are sent as null, not '', since the backend
            // fields are nullable Integers
            const payload = {
                initiative: initiativeInput.value === '' ? null : Number(initiativeInput.value),
                hp: hpInput ? (hpInput.value === '' ? null : Number(hpInput.value)) : null,
                maxHp: maxHpInput ? (maxHpInput.value === '' ? null : Number(maxHpInput.value)) : null,
                ac: acInput ? (acInput.value === '' ? null : Number(acInput.value)) : null,
                incapacitated: incapacitatedInput.checked
            };

            const updated = await updateCombatant(combatantId, payload);

            // keep data attributes in sync with the server response so turn-skipping
            // and initiative-sorting reflect the latest saved state, not stale values
            tile.dataset.incapacitated = updated.incapacitated;
            tile.dataset.initiative = updated.initiative === null ? '' : updated.initiative;
            tile.classList.toggle('incapacitated', updated.incapacitated);
            highlightCurrentTurn();
            sortTilesByInitiative();
        });
    }

    // notes render once at init time (not inside an event handler) since they
    // should be visible immediately, not only after some other action happens
    const noteSection = tile.querySelector('.note-section');
    if (noteSection) {
        renderNoteDisplay(noteSection, tile, noteSection.dataset.notes || null);
    }

    const deleteBtn = tile.querySelector('.delete-combatant-btn');
    if (deleteBtn) {
        deleteBtn.addEventListener('click', async () => {
            if (!confirm('Remove this combatant from the encounter?')) return;

            const combatantId = tile.dataset.combatantId;
            await deleteCombatantRequest(combatantId);

            // if the deleted combatant was the current turn, clear it client-side too.
            // (the DB's current_turn_id FK is ON DELETE SET NULL, so this just keeps
            // the JS variable in sync with what already happened server-side)
            const wasCurrentTurn = tile.dataset.combatantId === String(currentTurnId);
            tile.remove();

            if (wasCurrentTurn) {
                currentTurnId = null;
                highlightCurrentTurn();
            }
        });
    }

    const addStatusBtn = tile.querySelector('.add-status-btn');
    if (addStatusBtn) {
        addStatusBtn.addEventListener('click', () => openStatusModal(tile));
    }

    tile.querySelectorAll('.delete-status-btn').forEach(attachDeleteHandler);
}

async function deleteCombatantRequest(combatantId) {
    return apiFetch(`/api/combatants/${combatantId}`, { method: 'DELETE' });
}

// Builds a new tile's HTML from scratch in JS. This has to be kept in sync
// manually with the Thymeleaf template that renders tiles server-side -
// if a field is added/changed there, it needs to be mirrored here too.
function buildCombatantTile(data) {
    const tile = document.createElement('div');
    tile.className = 'combatant-tile' + (data.incapacitated ? ' incapacitated' : '');
    tile.dataset.combatantId = data.id;
    tile.dataset.incapacitated = data.incapacitated;
    tile.dataset.initiative = data.initiative === null ? '' : data.initiative;

    // players don't get HP/AC fields since they track their own HP
    const hpBlock = data.player ? '' : `
        <div>
            <label class="hp-label">HP
                <span class="hp-slash-group">
                    <input type="number" class="hp-input" value="${data.hp ?? ''}">
                    <span>/</span>
                    <input type="number" class="maxhp-input" value="${data.maxHp ?? ''}">
                </span>
            </label>
            <label>AC
                <input type="number" class="ac-input" value="${data.ac ?? ''}">
            </label>
        </div>
    `;

    tile.innerHTML = `
        <button type="button" class="delete-combatant-btn" title="Remove combatant">&times;</button>
        <strong></strong>
        <span>${data.player ? ' (Player)' : ' (NPC)'}</span>
        <label>Initiative
            <input type="number" class="initiative-input" value="${data.initiative ?? ''}">
        </label>
        ${hpBlock}
        <label class="checkbox-label">
            <input type="checkbox" class="incapacitated-input" ${data.incapacitated ? 'checked' : ''}>
            Incapacitated
        </label>
        <button type="button" class="update-btn btn btn-primary btn-small">Update</button>
        <div class="statuses">
            <ul class="status-list"></ul>
            <button type="button" class="add-status-btn add-row-btn add-row-btn-sm">+ Status</button>
        </div>
        <div class="note-section"></div>
    `;

    // set via textContent, not innerHTML, so a combatant name can't be used
    // to inject markup/scripts into the page
    tile.querySelector('strong').textContent = data.name;

    return tile;
}

async function createCombatant(encounterId, data) {
    return apiFetch(`/api/encounters/${encounterId}/combatants`, {
        method: 'POST',
        body: JSON.stringify(data)
    });
}

// ===== Add Combatant Modal =====

const combatantModal = document.getElementById('combatant-modal');
const combatantNameInput = document.getElementById('combatant-modal-name');
const combatantInitiativeInput = document.getElementById('combatant-modal-initiative');
const combatantHpInput = document.getElementById('combatant-modal-hp');
const combatantMaxHpInput = document.getElementById('combatant-modal-maxhp');
const combatantAcInput = document.getElementById('combatant-modal-ac');
const combatantModalCancelBtn = document.getElementById('combatant-modal-cancel');
const combatantModalSaveBtn = document.getElementById('combatant-modal-save');
const addCombatantBtn = document.getElementById('add-combatant-btn');

function openCombatantModal() {
    combatantNameInput.value = '';
    combatantInitiativeInput.value = '';
    combatantHpInput.value = '';
    combatantMaxHpInput.value = '';
    combatantAcInput.value = '';
    combatantTemplateSelect.value = '';
    combatantModal.style.display = 'flex';
    combatantNameInput.focus();
}

function closeCombatantModal() {
    combatantModal.style.display = 'none';
}

addCombatantBtn.addEventListener('click', openCombatantModal);
combatantModalCancelBtn.addEventListener('click', closeCombatantModal);

combatantModal.addEventListener('click', (event) => {
    if (event.target === combatantModal) closeCombatantModal();
});

combatantModalSaveBtn.addEventListener('click', async () => {
    const name = combatantNameInput.value.trim();
    if (!name) return;

    const payload = {
        name: name,
        initiative: combatantInitiativeInput.value === '' ? null : Number(combatantInitiativeInput.value),
        hp: combatantHpInput.value === '' ? null : Number(combatantHpInput.value),
        maxHp: combatantMaxHpInput.value === '' ? null : Number(combatantMaxHpInput.value),
        ac: combatantAcInput.value === '' ? null : Number(combatantAcInput.value)
    };

    const newCombatant = await createCombatant(encounterId, payload);

    const tile = buildCombatantTile(newCombatant);
    document.querySelector('.combatant-grid').appendChild(tile);
    initTile(tile); // wire up the new tile's buttons - not automatic since it wasn't rendered by Thymeleaf
    sortTilesByInitiative();

    closeCombatantModal();
});

// ===== Notes =====
// One note per combatant. Rendered via a small state machine (display <-> editor)
// owned entirely by JS rather than duplicated in the Thymeleaf template, since
// there are only two states to manage and it keeps the markup in one place.

async function saveNoteRequest(combatantId, notes) {
    return apiFetch(`/api/combatants/${combatantId}/notes`, {
        method: 'PUT',
        body: JSON.stringify({ notes })
    });
}

async function deleteNoteRequest(combatantId) {
    return apiFetch(`/api/combatants/${combatantId}/notes`, { method: 'DELETE' });
}

// Shows either a "+ Note" button (no note yet) or the note itself with
// minimize/delete controls. Clicking the note text opens the editor.
function renderNoteDisplay(section, tile, notesText) {
    section.innerHTML = '';

    if (!notesText) {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'add-note-btn add-row-btn add-row-btn-sm';
        btn.title = 'Add Note';
        btn.textContent = '+ Note';
        btn.addEventListener('click', () => renderNoteEditor(section, tile, ''));
        section.appendChild(btn);
        return;
    }

    const block = document.createElement('div');
    block.className = 'note-block';
    block.innerHTML = `
        <div class="note-header">
            <button type="button" class="note-minimize-btn" title="Minimize">&#8722;</button>
            <button type="button" class="note-delete-btn" title="Delete note">&times;</button>
        </div>
        <p class="note-text"></p>
    `;
    block.querySelector('.note-text').textContent = notesText;

    // minimize is purely visual/client-side - resets on reload, no persistence needed
    block.querySelector('.note-minimize-btn').addEventListener('click', (e) => {
        block.classList.toggle('minimized');
        e.target.innerHTML = block.classList.contains('minimized') ? '&#43;' : '&#8722;';
    });

    block.querySelector('.note-delete-btn').addEventListener('click', async () => {
        await deleteNoteRequest(tile.dataset.combatantId);
        renderNoteDisplay(section, tile, null);
    });

    // clicking the note text itself opens the editor pre-filled, doubling
    // as the "edit" action without needing a separate edit button
    block.querySelector('.note-text').addEventListener('click', () => {
        renderNoteEditor(section, tile, notesText);
    });

    section.appendChild(block);
}

function renderNoteEditor(section, tile, currentText) {
    section.innerHTML = '';

    const wrapper = document.createElement('div');
    wrapper.className = 'note-editor-wrapper';
    wrapper.innerHTML = `
        <textarea class="note-editor" maxlength="1000"></textarea>
        <div class="note-editor-actions">
            <button type="button" class="note-cancel-btn btn btn-secondary btn-small">Cancel</button>
            <button type="button" class="note-save-btn btn btn-primary btn-small">Save</button>
        </div>
    `;

    const textarea = wrapper.querySelector('.note-editor');
    textarea.value = currentText || '';

    wrapper.querySelector('.note-cancel-btn').addEventListener('click', () => {
        renderNoteDisplay(section, tile, currentText || null);
    });

    wrapper.querySelector('.note-save-btn').addEventListener('click', async () => {
        const newText = textarea.value.trim();
        if (!newText) return; // blank saves are blocked - use delete to actually clear a note

        const updated = await saveNoteRequest(tile.dataset.combatantId, newText);
        renderNoteDisplay(section, tile, updated.notes);
    });

    section.appendChild(wrapper);
    textarea.focus();
}

// ===== Monster Templates (prefabs) =====
// Selecting a template pre-fills the Add Combatant modal's fields, all of
// which remain editable afterward except initiative (not templated - it's
// rolled fresh per encounter regardless of monster type).

const combatantTemplateSelect = document.getElementById('combatant-modal-template');

// Counts combatants in the current encounter whose name matches the template's
// base name (exact match or "Name <number>"), so a new one can be numbered
// "Goblin 3" etc. Scoped to this encounter only, not the whole campaign.
function countExistingWithBaseName(baseName) {
    const tiles = getTiles();
    let count = 0;
    for (const tile of tiles) {
        const tileName = tile.querySelector('strong').textContent;
        if (tileName === baseName || tileName.match(new RegExp(`^${escapeRegex(baseName)} \\d+$`))) {
            count++;
        }
    }
    return count;
}

// escapes regex special characters in a monster name before it's used to
// build a RegExp, in case the name ever contains something like "." or "("
function escapeRegex(str) {
    return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

combatantTemplateSelect.addEventListener('change', () => {
    const selected = combatantTemplateSelect.selectedOptions[0];

    if (!selected.value) {
        // "-- Custom --" selected - clear fields back to blank
        combatantNameInput.value = '';
        combatantHpInput.value = '';
        combatantMaxHpInput.value = '';
        combatantAcInput.value = '';
        return;
    }

    const baseName = selected.dataset.name;
    const existingCount = countExistingWithBaseName(baseName);

    combatantNameInput.value = `${baseName} ${existingCount + 1}`;
    combatantHpInput.value = selected.dataset.hp || '';
    combatantMaxHpInput.value = selected.dataset.maxhp || '';
    combatantAcInput.value = selected.dataset.ac || '';
});

// ===== Init =====
// Wire up every tile present at page load, then reflect whatever turn
// was saved server-side (if any).
getTiles().forEach(tile => initTile(tile))
highlightCurrentTurn();