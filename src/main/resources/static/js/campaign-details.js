// ---- Party Members ----

async function createPartyMember(name) {
    return apiFetch(`/api/campaigns/${campaignId}/party-members`, {
        method: 'POST',
        body: JSON.stringify({ name })
    });
}

async function deletePartyMemberRequest(id) {
    return apiFetch(`/api/party-members/${id}`, { method: 'DELETE' });
}

const memberModal = document.getElementById('member-modal');
const memberNameInput = document.getElementById('member-modal-name');
const memberModalCancelBtn = document.getElementById('member-modal-cancel');
const memberModalSaveBtn = document.getElementById('member-modal-save');

document.getElementById('add-member-btn').addEventListener('click', () => {
    memberNameInput.value = '';
    memberModal.style.display = 'flex';
    memberNameInput.focus();
});

memberModalCancelBtn.addEventListener('click', () => memberModal.style.display = 'none');
memberModal.addEventListener('click', (e) => {
    if (e.target === memberModal) memberModal.style.display = 'none';
});

memberModalSaveBtn.addEventListener('click', async () => {
    const name = memberNameInput.value.trim();
    if (!name) return;

    const newMember = await createPartyMember(name);

    const li = document.createElement('li');
    li.dataset.memberId = newMember.id;
    li.innerHTML = `
        <div class="item-info">
            <span class="member-name"></span>
        </div>
        <div class="item-actions">
            <form method="post">
                <button type="submit" class="btn btn-secondary btn-small">Set Inactive</button>
            </form>
            <button type="button" class="btn btn-danger btn-small delete-member-btn">Delete</button>
        </div>
    `;
    li.querySelector('.member-name').textContent = newMember.name;
    li.querySelector('form').action = `/party-members/${newMember.id}/toggle-active`;
    attachMemberDeleteHandler(li.querySelector('.delete-member-btn'));

    document.getElementById('party-member-list').appendChild(li);
    memberModal.style.display = 'none';
});

function attachMemberDeleteHandler(button) {
    button.addEventListener('click', async () => {
        const li = button.closest('li');
        const id = li.dataset.memberId;

        await deletePartyMemberRequest(id);
        li.remove();
    });
}

document.querySelectorAll('.delete-member-btn').forEach(attachMemberDeleteHandler);

// ---- Encounters ----

async function createEncounterRequest(name) {
    return apiFetch(`/api/campaigns/${campaignId}/encounters`, {
        method: 'POST',
        body: JSON.stringify({ name })
    });
}

async function deleteEncounterRequest(id) {
    return apiFetch(`/api/encounters/${id}`, { method: 'DELETE' });
}

const encounterModal = document.getElementById('encounter-modal');
const encounterNameInput = document.getElementById('encounter-modal-name');
const encounterModalCancelBtn = document.getElementById('encounter-modal-cancel');
const encounterModalSaveBtn = document.getElementById('encounter-modal-save');

document.getElementById('add-encounter-btn').addEventListener('click', () => {
    encounterNameInput.value = '';
    encounterModal.style.display = 'flex';
    encounterNameInput.focus();
});

encounterModalCancelBtn.addEventListener('click', () => encounterModal.style.display = 'none');
encounterModal.addEventListener('click', (e) => {
    if (e.target === encounterModal) encounterModal.style.display = 'none';
});

encounterModalSaveBtn.addEventListener('click', async () => {
    const name = encounterNameInput.value.trim();
    if (!name) return;

    const newEncounter = await createEncounterRequest(name);
    window.location.href = `/encounters/${newEncounter.id}`;
});

function attachEncounterDeleteHandler(button) {
    button.addEventListener('click', async () => {
        if (!confirm('Delete this encounter and all its combatants?')) return;

        const li = button.closest('li');
        const id = li.dataset.encounterId;

        await deleteEncounterRequest(id);
        li.remove();
    });
}

// Makes the whole row navigate to the encounter, except clicks on the delete button
document.querySelectorAll('#encounter-list li.clickable-row').forEach(li => {
    li.addEventListener('click', (e) => {
        if (e.target.closest('.delete-encounter-btn')) return; // let delete handle its own click, don't navigate
        const link = li.querySelector('a');
        window.location.href = link.getAttribute('href');
    });
});

document.querySelectorAll('.delete-encounter-btn').forEach(attachEncounterDeleteHandler);

// ---- Delete Campaign ----

const deleteCampaignModal = document.getElementById('delete-campaign-modal');
const deleteCampaignInput = document.getElementById('delete-campaign-input');
const deleteCampaignCancelBtn = document.getElementById('delete-campaign-cancel');
const deleteCampaignConfirmBtn = document.getElementById('delete-campaign-confirm');

document.getElementById('delete-campaign-btn').addEventListener('click', () => {
    deleteCampaignInput.value = '';
    deleteCampaignConfirmBtn.disabled = true;
    deleteCampaignModal.style.display = 'flex';
    deleteCampaignInput.focus();
});

deleteCampaignCancelBtn.addEventListener('click', () => deleteCampaignModal.style.display = 'none');
deleteCampaignModal.addEventListener('click', (e) => {
    if (e.target === deleteCampaignModal) deleteCampaignModal.style.display = 'none';
});

deleteCampaignInput.addEventListener('input', () => {
    deleteCampaignConfirmBtn.disabled = deleteCampaignInput.value !== campaignName;
});

deleteCampaignConfirmBtn.addEventListener('click', async () => {
    await apiFetch(`/api/campaigns/${campaignId}`, { method: 'DELETE' });
    window.location.href = '/campaigns';
});

async function createTemplateRequest(data) {
    return apiFetch(`/api/campaigns/${campaignId}/monster-templates`, {
        method: 'POST',
        body: JSON.stringify(data)
    });
}

async function deleteTemplateRequest(id) {
    return apiFetch(`/api/monster-templates/${id}`, { method: 'DELETE' });
}

const templateModal = document.getElementById('template-modal');
const templateNameInput = document.getElementById('template-modal-name');
const templateHpInput = document.getElementById('template-modal-hp');
const templateMaxHpInput = document.getElementById('template-modal-maxhp');
const templateAcInput = document.getElementById('template-modal-ac');
const templateModalCancelBtn = document.getElementById('template-modal-cancel');
const templateModalSaveBtn = document.getElementById('template-modal-save');

document.getElementById('add-template-btn').addEventListener('click', () => {
    templateNameInput.value = '';
    templateHpInput.value = '';
    templateMaxHpInput.value = '';
    templateAcInput.value = '';
    templateModal.style.display = 'flex';
    templateNameInput.focus();
});

templateModalCancelBtn.addEventListener('click', () => templateModal.style.display = 'none');
templateModal.addEventListener('click', (e) => {
    if (e.target === templateModal) templateModal.style.display = 'none';
});

templateModalSaveBtn.addEventListener('click', async () => {
    const name = templateNameInput.value.trim();
    if (!name) return;

    const newTemplate = await createTemplateRequest({
        name,
        hp: templateHpInput.value === '' ? null : Number(templateHpInput.value),
        maxHp: templateMaxHpInput.value === '' ? null : Number(templateMaxHpInput.value),
        ac: templateAcInput.value === '' ? null : Number(templateAcInput.value)
    });

    const li = document.createElement('li');
    li.dataset.templateId = newTemplate.id;
    li.innerHTML = `
        <div class="item-info">
            <span></span>
            <span class="item-meta"></span>
        </div>
        <div class="item-actions">
            <button type="button" class="btn btn-danger btn-small delete-template-btn">Delete</button>
        </div>
    `;
    li.querySelectorAll('span')[0].textContent = newTemplate.name;
    li.querySelectorAll('span')[1].textContent =
        ` (HP: ${newTemplate.hp ?? '-'}/${newTemplate.maxHp ?? '-'}, AC: ${newTemplate.ac ?? '-'})`;
    attachTemplateDeleteHandler(li.querySelector('.delete-template-btn'));

    document.getElementById('template-list').appendChild(li);
    templateModal.style.display = 'none';
});

function attachTemplateDeleteHandler(button) {
    button.addEventListener('click', async () => {
        const li = button.closest('li');
        await deleteTemplateRequest(li.dataset.templateId);
        li.remove();
    });
}

document.querySelectorAll('.delete-template-btn').forEach(attachTemplateDeleteHandler);