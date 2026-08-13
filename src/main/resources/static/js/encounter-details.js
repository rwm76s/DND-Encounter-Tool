let currentTurnId = initialTurnId;

function getTiles() {
  return Array.from(document.querySelectorAll('.combatant-tile'));
}

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

function findNextLivingIndex(tiles, startIndex, direction) {
  if (tiles.length === 0) return -1;

  let index = startIndex;
  for (let attempts = 0; attempts < tiles.length; attempts++) {
      index = ((index + direction) % tiles.length + tiles.length) % tiles.length;
      if (tiles[index].dataset.incapacitated !== 'true') {
          return index;
      }
  }
  return -1;
}

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

function sortTilesByInitiative() {
  const grid = document.querySelector('.combatant-grid');
  const tileArray = getTiles();

  tileArray.sort((a, b) => {
      const initA = a.dataset.initiative === '' || a.dataset.initiative === undefined
          ? null : Number(a.dataset.initiative);
      const initB = b.dataset.initiative === '' || b.dataset.initiative === undefined
          ? null : Number(b.dataset.initiative);

      if (initA === null && initB === null) return 0;
      if (initA === null) return 1;
      if (initB === null) return -1;
      return initB - initA;
  });

  tileArray.forEach(tile => grid.appendChild(tile));
}

async function updateCombatant(combatantId, data) {
  return apiFetch(`/api/combatants/${combatantId}`, {
      method: 'PUT',
      body: JSON.stringify(data)
  });
}

async function addStatusRequest(combatantId, statusText) {
return apiFetch(`/api/combatants/${combatantId}/statuses`, {
    method: 'POST',
    body: JSON.stringify({ status: statusText })
});
}

async function deleteStatusRequest(statusId) {
  return apiFetch(`/api/statuses/${statusId}`, { method: 'DELETE' });
}

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
  li.querySelector('span').textContent = newStatus.status;
  attachDeleteHandler(li.querySelector('.delete-status-btn'));
  list.appendChild(li);

  refreshStatusLabel(modalTargetTile);
  closeStatusModal();
});

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

        const payload = {
            initiative: initiativeInput.value === '' ? null : Number(initiativeInput.value),
            hp: hpInput ? (hpInput.value === '' ? null : Number(hpInput.value)) : null,
            maxHp: maxHpInput ? (maxHpInput.value === '' ? null : Number(maxHpInput.value)) : null,
            ac: acInput ? (acInput.value === '' ? null : Number(acInput.value)) : null,
            incapacitated: incapacitatedInput.checked
        };

        const updated = await updateCombatant(combatantId, payload);

        tile.dataset.incapacitated = updated.incapacitated;
        tile.dataset.initiative = updated.initiative === null ? '' : updated.initiative;
        tile.classList.toggle('incapacitated', updated.incapacitated);
        highlightCurrentTurn();
        sortTilesByInitiative();
    });
}

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

        const wasCurrentTurn = tile.dataset.combatantId === String(currentTurnId);
        tile.remove();

        if (wasCurrentTurn) {
            currentTurnId = null;
            highlightCurrentTurn();
        }
    });
}

async function deleteCombatantRequest(combatantId) {
  return apiFetch(`/api/combatants/${combatantId}`, { method: 'DELETE' });
}

const addStatusBtn = tile.querySelector('.add-status-btn');
if (addStatusBtn) {
    addStatusBtn.addEventListener('click', () => openStatusModal(tile));
}

tile.querySelectorAll('.delete-status-btn').forEach(attachDeleteHandler);
}

function buildCombatantTile(data) {
const tile = document.createElement('div');
tile.className = 'combatant-tile' + (data.incapacitated ? ' incapacitated' : '');
tile.dataset.combatantId = data.id;
tile.dataset.incapacitated = data.incapacitated;
tile.dataset.initiative = data.initiative === null ? '' : data.initiative;

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

tile.querySelector('strong').textContent = data.name;

return tile;
}

async function createCombatant(encounterId, data) {
return apiFetch(`/api/encounters/${encounterId}/combatants`, {
    method: 'POST',
    body: JSON.stringify(data)
});
}

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
initTile(tile);
sortTilesByInitiative();

closeCombatantModal();
});

async function saveNoteRequest(combatantId, notes) {
return apiFetch(`/api/combatants/${combatantId}/notes`, {
    method: 'PUT',
    body: JSON.stringify({ notes })
});
}

async function deleteNoteRequest(combatantId) {
return apiFetch(`/api/combatants/${combatantId}/notes`, { method: 'DELETE' });
}

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

block.querySelector('.note-minimize-btn').addEventListener('click', (e) => {
    block.classList.toggle('minimized');
    e.target.innerHTML = block.classList.contains('minimized') ? '&#43;' : '&#8722;';
});

block.querySelector('.note-delete-btn').addEventListener('click', async () => {
    await deleteNoteRequest(tile.dataset.combatantId);
    renderNoteDisplay(section, tile, null);
});

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
    if (!newText) return;

    const updated = await saveNoteRequest(tile.dataset.combatantId, newText);
    renderNoteDisplay(section, tile, updated.notes);
});

section.appendChild(wrapper);
textarea.focus();
}



const combatantTemplateSelect = document.getElementById('combatant-modal-template');

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

function escapeRegex(str) {
return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

combatantTemplateSelect.addEventListener('change', () => {
const selected = combatantTemplateSelect.selectedOptions[0];

if (!selected.value) {
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

getTiles().forEach(tile => initTile(tile))
highlightCurrentTurn();