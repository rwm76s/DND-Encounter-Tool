const csrfToken = document.querySelector('meta[name="_csrf"]').content;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

async function apiFetch(url, options = {}) {
     try {
         const headers = {
             'Content-Type': 'application/json',
             [csrfHeader]: csrfToken,
             ...(options.headers || {})
         };

         const response = await fetch(url, { ...options, headers });

         if (!response.ok) {
             const text = await response.text();
             throw new Error(`Request failed (${response.status}): ${text}`);
         }

         if (response.status === 204) return null;
         return await response.json();
     } catch (err) {
         console.error('apiFetch error:', err);
         alert('Something went wrong: ' + err.message);
         throw err;
     }
}