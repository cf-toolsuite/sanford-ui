window.audioRecorder = {
    mediaRecorder: null,
    audioChunks: [],

    startRecording: async function() {
        try {
            const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
            this.mediaRecorder = new MediaRecorder(stream);
            this.audioChunks = [];

            this.mediaRecorder.ondataavailable = (event) => {
                if (event.data.size > 0) {
                    this.audioChunks.push(event.data);
                }
            };

            this.mediaRecorder.start();
        } catch (error) {
            console.error('Error accessing microphone:', error);
        }
    },

    stopRecording: async function() {
        return new Promise((resolve, reject) => {
            if (this.mediaRecorder && this.mediaRecorder.state !== 'inactive') {
                this.mediaRecorder.stop();
                this.mediaRecorder.onstop = async () => {
                    try {
                        const audioBlob = new Blob(this.audioChunks, { type: 'audio/webm' });
                        const base64Data = await this.blobToBase64(audioBlob);
                        
                        // Stop all tracks
                        this.mediaRecorder.stream.getTracks().forEach(track => track.stop());
                        
                        resolve({ audioData: base64Data });
                    } catch (error) {
                        reject(error);
                    }
                };
            } else {
                reject(new Error('No recording in progress'));
            }
        });
    },

    blobToBase64: function(blob) {
        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.onloadend = () => {
                const base64String = reader.result
                    .replace('data:audio/webm;base64,', '');
                resolve(base64String);
            };
            reader.onerror = reject;
            reader.readAsDataURL(blob);
        });
    },

    playAudioResponse: function(base64Audio) {
        const audio = new Audio(`data:audio/wav;base64,${base64Audio}`);
        audio.play();
    }
};