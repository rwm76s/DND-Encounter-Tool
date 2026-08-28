// Sends the new campaign to the server; ownership is implicit since the
// backend attaches whichever user is currently authenticated.
async function createCampaignRequest(name) {
    return apiFetch('/api/campaigns', {
        method: 'POST',
        body: JSON.stringify({ name })
    });
}

const campaignModal = document.getElementById('campaign-modal');
const campaignNameInput = document.getElementById('campaign-modal-name');
const campaignModalCancelBtn = document.getElementById('campaign-modal-cancel');
const campaignModalSaveBtn = document.getElementById('campaign-modal-save');

document.getElementById('add-campaign-btn').addEventListener('click', () => {
    campaignNameInput.value = '';
    campaignModal.style.display = 'flex';
    campaignNameInput.focus();
});

campaignModalCancelBtn.addEventListener('click', () => campaignModal.style.display = 'none');

// clicking the dark overlay (but not the box itself) closes the modal -
// event.target is only the overlay when the click didn't land on a child element
campaignModal.addEventListener('click', (e) => {
    if (e.target === campaignModal) campaignModal.style.display = 'none';
});

campaignModalSaveBtn.addEventListener('click', async () => {
    const name = campaignNameInput.value.trim();
    if (!name) return;

    const newCampaign = await createCampaignRequest(name);

    // redirect straight into the new campaign rather than inserting a tile
    // into the grid, so this avoids needing to duplicate the tile building
    // logic into this page.
    window.location.href = `/campaigns/${newCampaign.id}`;
});