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
campaignModal.addEventListener('click', (e) => {
    if (e.target === campaignModal) campaignModal.style.display = 'none';
});

campaignModalSaveBtn.addEventListener('click', async () => {
    const name = campaignNameInput.value.trim();
    if (!name) return;

    const newCampaign = await createCampaignRequest(name);
    window.location.href = `/campaigns/${newCampaign.id}`;
});