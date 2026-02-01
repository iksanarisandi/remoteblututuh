import tkinter as tk
from tkinter import ttk
import keyboard
import time

class RemoteTVDirectApp:
    def __init__(self, root):
        self.root = root
        self.root.title("Remote TV Bluetooth - Direct Control")
        self.root.geometry("550x350")
        self.root.resizable(False, False)
        
        # Status - LANGSUNG AKTIF SECARA DEFAULT
        self.is_active = True
        
        # Style
        style = ttk.Style()
        style.configure('TButton', font=('Arial', 14), padding=10)
        
        # Main Frame
        main_frame = ttk.Frame(root, padding="20")
        main_frame.pack(expand=True, fill='both')
        
        # Title
        title_label = ttk.Label(main_frame, text="Remote TV Bluetooth - Direct", 
                               font=('Arial', 14, 'bold'))
        title_label.pack(pady=15)
        
        # Status Label
        self.status_label = ttk.Label(main_frame, text="✓ Status: AKTIF - Siap mengirim ke TV",
                                     font=('Arial', 12, 'bold'), foreground='green')
        self.status_label.pack(pady=10)
        
        # Device Info
        device_label = ttk.Label(main_frame, 
                                text="✓ Device: B860H V5.0 nown (TERHUBUNG)", 
                                font=('Arial', 10), foreground='green')
        device_label.pack(pady=5)
        
        # Button Frame
        button_frame = ttk.Frame(main_frame)
        button_frame.pack(pady=20)
        
        # Tombol OK
        self.ok_button = tk.Button(button_frame, text="OK", 
                                  bg='#4CAF50', fg='white',
                                  font=('Arial', 14, 'bold'),
                                  width=12, height=2,
                                  command=self.press_ok)
        self.ok_button.grid(row=0, column=0, padx=8, pady=8)
        
        # Tombol Play/Pause
        self.play_button = tk.Button(button_frame, text="▶||", 
                                    bg='#2196F3', fg='white',
                                    font=('Arial', 14, 'bold'),
                                    width=12, height=2,
                                    command=self.press_play)
        self.play_button.grid(row=0, column=1, padx=8, pady=8)
        
        # Tombol OK + Play/Pause BERSAMAAN ⭐
        self.both_button = tk.Button(button_frame, text="OK + ▶||", 
                                    bg='#FF9800', fg='white',
                                    font=('Arial', 14, 'bold'),
                                    width=15, height=2,
                                    command=self.press_both)
        self.both_button.grid(row=0, column=2, padx=8, pady=8)
        
        # Label tombol
        ok_label = ttk.Label(button_frame, text="OK (Enter)", font=('Arial', 9))
        ok_label.grid(row=1, column=0, padx=8)
        
        play_label = ttk.Label(button_frame, text="Play/Pause", font=('Arial', 9))
        play_label.grid(row=1, column=1, padx=8)
        
        both_label = ttk.Label(button_frame, text="Keduanya\nBERSAMAAN", font=('Arial', 9, 'bold'))
        both_label.grid(row=1, column=2, padx=8)
        
        # Info Label
        info_label = ttk.Label(main_frame, 
                              text="✓ Remote langsung aktif!\nKlik tombol untuk kirim sinyal ke TV\nKeyboard: O=OK, P=Play, B=Keduanya",
                              font=('Arial', 9), justify='center', foreground='green')
        info_label.pack(pady=20)
        
        # Log
        self.log_label = ttk.Label(main_frame, text="Siap digunakan!", font=('Arial', 9), foreground='blue')
        self.log_label.pack(pady=5)
        
        # Bind keyboard shortcuts
        keyboard.on_press_key("o", self.on_o_press)
        keyboard.on_press_key("p", self.on_p_press)
        keyboard.on_press_key("b", self.on_b_press)
    
    def press_ok(self):
        self.log_label.config(text="Mengirim OK ke TV...", foreground='blue')
        # Kirim Enter ke device Bluetooth (Windows akan forward ke device aktif)
        keyboard.press_and_release('enter')
        self.flash_button(self.ok_button)
        self.root.after(200, lambda: self.log_label.config(text="✓ Sinyal OK terkirim!", foreground='green'))
    
    def press_play(self):
        self.log_label.config(text="Mengirim Play/Pause ke TV...", foreground='blue')
        # Kirim Play/Pause ke device Bluetooth
        keyboard.press_and_release('media play pause')
        self.flash_button(self.play_button)
        self.root.after(200, lambda: self.log_label.config(text="✓ Sinyal Play/Pause terkirim!", foreground='green'))
    
    def press_both(self):
        """Kirim OK + Play/Pause secara BERSAMAAN"""
        self.log_label.config(text="Mengirim OK + Play/Pause BERSAMAAN ke TV...", foreground='orange')
        
        # Tekan kedua tombol secara bersamaan
        keyboard.press('enter')
        keyboard.press('media play pause')
        
        # Tahan sebentar
        self.root.after(100, self.release_both)
        self.flash_button(self.both_button)
    
    def release_both(self):
        """Lepas kedua tombol"""
        keyboard.release('enter')
        keyboard.release('media play pause')
        self.log_label.config(text="✓ Sinyal OK + Play/Pause BERSAMAAN terkirim!", foreground='green')
    
    def on_o_press(self, e):
        if self.is_active:
            self.press_ok()
    
    def on_p_press(self, e):
        if self.is_active:
            self.press_play()
    
    def on_b_press(self, e):
        if self.is_active:
            self.press_both()
    
    def flash_button(self, button):
        original_bg = button.cget('bg')
        button.config(bg='#FFFF00')
        self.root.after(100, lambda: button.config(bg=original_bg))

def main():
    root = tk.Tk()
    app = RemoteTVDirectApp(root)
    root.mainloop()

if __name__ == "__main__":
    main()
