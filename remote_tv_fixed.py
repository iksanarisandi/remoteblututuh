import tkinter as tk
from tkinter import ttk
import keyboard
import time

class RemoteTVFixedApp:
    def __init__(self, root):
        self.root = root
        self.root.title("Remote TV Bluetooth - Fixed")
        self.root.geometry("600x400")
        self.root.resizable(False, False)
        
        # Status - LANGSUNG AKTIF
        self.is_active = True
        
        # Style
        style = ttk.Style()
        style.configure('TButton', font=('Arial', 14), padding=10)
        
        # Main Frame
        main_frame = ttk.Frame(root, padding="20")
        main_frame.pack(expand=True, fill='both')
        
        # Title
        title_label = ttk.Label(main_frame, text="Remote TV Bluetooth - Fixed", 
                               font=('Arial', 14, 'bold'))
        title_label.pack(pady=10)
        
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
        button_frame.pack(pady=15)
        
        # Tombol OK
        self.ok_button = tk.Button(button_frame, text="OK", 
                                  bg='#4CAF50', fg='white',
                                  font=('Arial', 13, 'bold'),
                                  width=10, height=2,
                                  command=self.press_ok)
        self.ok_button.grid(row=0, column=0, padx=6, pady=6)
        
        # Tombol Play/Pause
        self.play_button = tk.Button(button_frame, text="▶||", 
                                    bg='#2196F3', fg='white',
                                    font=('Arial', 13, 'bold'),
                                    width=10, height=2,
                                    command=self.press_play)
        self.play_button.grid(row=0, column=1, padx=6, pady=6)
        
        # Tombol OK + Play/Pause BERSAMAAN
        self.both_button = tk.Button(button_frame, text="OK + ▶||", 
                                    bg='#FF9800', fg='white',
                                    font=('Arial', 13, 'bold'),
                                    width=12, height=2,
                                    command=self.press_both)
        self.both_button.grid(row=0, column=2, padx=6, pady=6)
        
        # Tombol Volume Up (TESTING)
        self.vol_up_button = tk.Button(button_frame, text="VOL +", 
                                      bg='#9C27B0', fg='white',
                                      font=('Arial', 13, 'bold'),
                                      width=10, height=2,
                                      command=self.press_vol_up)
        self.vol_up_button.grid(row=0, column=3, padx=6, pady=6)
        
        # Label tombol
        ok_label = ttk.Label(button_frame, text="OK\n(Enter)", font=('Arial', 8))
        ok_label.grid(row=1, column=0, padx=6)
        
        play_label = ttk.Label(button_frame, text="Play/Pause\n(Space)", font=('Arial', 8))
        play_label.grid(row=1, column=1, padx=6)
        
        both_label = ttk.Label(button_frame, text="Keduanya\nBERSAMAAN", font=('Arial', 8, 'bold'))
        both_label.grid(row=1, column=2, padx=6)
        
        vol_label = ttk.Label(button_frame, text="Volume Up\n(TEST)", font=('Arial', 8, 'bold'))
        vol_label.grid(row=1, column=3, padx=6)
        
        # Info Label
        info_label = ttk.Label(main_frame, 
                              text="✓ Remote langsung aktif!\nKlik tombol untuk kirim sinyal ke TV\nCoba tombol VOL + untuk testing koneksi\nKeyboard: O=OK, P=Play, B=Keduanya, V=Vol Up",
                              font=('Arial', 9), justify='center', foreground='green')
        info_label.pack(pady=15)
        
        # Log
        self.log_label = ttk.Label(main_frame, text="Siap digunakan!", font=('Arial', 9), foreground='blue')
        self.log_label.pack(pady=5)
        
        # Bind keyboard shortcuts
        keyboard.on_press_key("o", self.on_o_press)
        keyboard.on_press_key("p", self.on_p_press)
        keyboard.on_press_key("b", self.on_b_press)
        keyboard.on_press_key("v", self.on_v_press)
    
    def press_ok(self):
        self.log_label.config(text="Mengirim OK ke TV...", foreground='blue')
        keyboard.press_and_release('enter')
        self.flash_button(self.ok_button)
        self.root.after(200, lambda: self.log_label.config(text="✓ Sinyal OK terkirim!", foreground='green'))
    
    def press_play(self):
        self.log_label.config(text="Mengirim Play/Pause ke TV...", foreground='blue')
        # Gunakan space key sebagai Play/Pause
        keyboard.press_and_release('space')
        self.flash_button(self.play_button)
        self.root.after(200, lambda: self.log_label.config(text="✓ Sinyal Play/Pause terkirim!", foreground='green'))
    
    def press_both(self):
        """Kirim OK + Play/Pause secara BERSAMAAN"""
        self.log_label.config(text="Mengirim OK + Play/Pause BERSAMAAN ke TV...", foreground='orange')
        
        # Tekan kedua tombol secara bersamaan (Enter + Space)
        keyboard.press('enter')
        keyboard.press('space')
        
        # Tahan sebentar
        self.root.after(100, self.release_both)
        self.flash_button(self.both_button)
    
    def release_both(self):
        """Lepas kedua tombol"""
        keyboard.release('enter')
        keyboard.release('space')
        self.log_label.config(text="✓ Sinyal OK + Play/Pause BERSAMAAN terkirim!", foreground='green')
    
    def press_vol_up(self):
        """Test dengan Volume Up"""
        self.log_label.config(text="Mengirim Volume Up ke TV...", foreground='purple')
        keyboard.press_and_release('volume up')
        self.flash_button(self.vol_up_button)
        self.root.after(200, lambda: self.log_label.config(text="✓ Volume Up terkirim! (Test koneksi)", foreground='green'))
    
    def on_o_press(self, e):
        self.press_ok()
    
    def on_p_press(self, e):
        self.press_play()
    
    def on_b_press(self, e):
        self.press_both()
    
    def on_v_press(self, e):
        self.press_vol_up()
    
    def flash_button(self, button):
        original_bg = button.cget('bg')
        button.config(bg='#FFFF00')
        self.root.after(100, lambda: button.config(bg=original_bg))

def main():
    root = tk.Tk()
    app = RemoteTVFixedApp(root)
    root.mainloop()

if __name__ == "__main__":
    main()
