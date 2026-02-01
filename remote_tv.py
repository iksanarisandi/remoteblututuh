import tkinter as tk
from tkinter import ttk
import keyboard
import threading

class RemoteTVApp:
    def __init__(self, root):
        self.root = root
        self.root.title("Remote TV Bluetooth")
        self.root.geometry("650x400")
        self.root.resizable(False, False)
        
        # Status
        self.is_active = False
        
        # Style
        style = ttk.Style()
        style.configure('TButton', font=('Arial', 14), padding=10)
        style.configure('Active.TButton', font=('Arial', 14, 'bold'), padding=10)
        
        # Main Frame
        main_frame = ttk.Frame(root, padding="20")
        main_frame.pack(expand=True, fill='both')
        
        # Title
        title_label = ttk.Label(main_frame, text="Remote TV Bluetooth", 
                               font=('Arial', 16, 'bold'))
        title_label.pack(pady=20)
        
        # Status Label
        self.status_label = ttk.Label(main_frame, text="Status: NONAKTIF", 
                                     font=('Arial', 12), foreground='red')
        self.status_label.pack(pady=10)
        
        # Button Frame
        button_frame = ttk.Frame(main_frame)
        button_frame.pack(pady=20)
        
        # Tombol OK
        self.ok_button = tk.Button(button_frame, text="OK", 
                                  bg='#4CAF50', fg='white',
                                  font=('Arial', 14, 'bold'),
                                  width=12, height=2,
                                  command=self.press_ok)
        self.ok_button.grid(row=0, column=0, padx=10, pady=10)
        
        # Tombol Play/Pause
        self.play_button = tk.Button(button_frame, text="▶||", 
                                    bg='#2196F3', fg='white',
                                    font=('Arial', 14, 'bold'),
                                    width=12, height=2,
                                    command=self.press_play)
        self.play_button.grid(row=0, column=1, padx=10, pady=10)
        
        # Tombol KOMBINASI (OK + Play/Pause)
        self.both_button = tk.Button(button_frame, text="OK + ▶||", 
                                    bg='#9C27B0', fg='white',
                                    font=('Arial', 14, 'bold'),
                                    width=12, height=2,
                                    command=self.press_both)
        self.both_button.grid(row=0, column=2, padx=10, pady=10)
        
        # Tombol Volume Up (TESTING)
        self.vol_up_button = tk.Button(button_frame, text="VOL +", 
                                      bg='#FF5722', fg='white',
                                      font=('Arial', 14, 'bold'),
                                      width=12, height=2,
                                      command=self.press_vol_up)
        self.vol_up_button.grid(row=0, column=3, padx=10, pady=10)
        
        # Label tombol
        ok_label = ttk.Label(button_frame, text="OK", font=('Arial', 10))
        ok_label.grid(row=1, column=0, padx=10)
        
        play_label = ttk.Label(button_frame, text="Play/Pause", font=('Arial', 10))
        play_label.grid(row=1, column=1, padx=10)
        
        both_label = ttk.Label(button_frame, text="Keduanya", font=('Arial', 10))
        both_label.grid(row=1, column=2, padx=10)
        
        vol_label = ttk.Label(button_frame, text="Vol Up (Test)", font=('Arial', 10))
        vol_label.grid(row=1, column=3, padx=10)
        
        # Activate Button
        self.activate_button = tk.Button(main_frame, text="AKTIFKAN", 
                                        bg='#FF9800', fg='white',
                                        font=('Arial', 12, 'bold'),
                                        width=20, height=1,
                                        command=self.toggle_active)
        self.activate_button.pack(pady=20)
        
        # Info Label
        info_label = ttk.Label(main_frame, 
                              text="Klik AKTIFKAN untuk mulai menggunakan remote\nKlik tombol untuk mengirim sinyal\nKeyboard: O=OK, P=Play/Pause, B=Keduanya, V=Vol Up",
                              font=('Arial', 9), justify='center')
        info_label.pack(pady=10)
        
        # Log
        self.log_label = ttk.Label(main_frame, text="", font=('Arial', 9))
        self.log_label.pack(pady=5)
        
        # Bind keyboard shortcuts
        keyboard.on_press_key("o", self.on_o_press)
        keyboard.on_press_key("p", self.on_p_press)
        keyboard.on_press_key("b", self.on_b_press)
        keyboard.on_press_key("v", self.on_v_press)
        
    def toggle_active(self):
        self.is_active = not self.is_active
        
        if self.is_active:
            self.status_label.config(text="Status: AKTIF", foreground='green')
            self.activate_button.config(text="NONAKTIFKAN", bg='#f44336')
            self.log_label.config(text="Remote siap digunakan!")
        else:
            self.status_label.config(text="Status: NONAKTIF", foreground='red')
            self.activate_button.config(text="AKTIFKAN", bg='#FF9800')
            self.log_label.config(text="")
    
    def press_ok(self):
        if self.is_active:
            self.log_label.config(text="Tombol OK ditekan!")
            # Simulate keyboard press - Enter as OK
            keyboard.press_and_release('enter')
            self.flash_button(self.ok_button)
        else:
            self.log_label.config(text="Aktifkan remote terlebih dahulu!")
    
    def press_play(self):
        if self.is_active:
            self.log_label.config(text="Tombol Play/Pause ditekan!")
            # Simulate keyboard press - Space as Play/Pause
            keyboard.press_and_release('space')
            self.flash_button(self.play_button)
        else:
            self.log_label.config(text="Aktifkan remote terlebih dahulu!")
    
    def on_o_press(self, e):
        if self.is_active:
            self.press_ok()
    
    def on_p_press(self, e):
        if self.is_active:
            self.press_play()
    
    def press_both(self):
        if self.is_active:
            self.log_label.config(text="Kedua tombol ditekan bersamaan!")
            # Tekan kedua tombol secara bersamaan
            keyboard.press('enter')
            keyboard.press('space')
            self.root.after(50, lambda: self.release_both())
            self.flash_button(self.both_button)
        else:
            self.log_label.config(text="Aktifkan remote terlebih dahulu!")
    
    def release_both(self):
        keyboard.release('enter')
        keyboard.release('space')
    
    def on_b_press(self, e):
        if self.is_active:
            self.press_both()
    
    def press_vol_up(self):
        if self.is_active:
            self.log_label.config(text="Tombol Volume Up ditekan!")
            # Simulate keyboard press - Volume up
            keyboard.press_and_release('volume up')
            self.flash_button(self.vol_up_button)
        else:
            self.log_label.config(text="Aktifkan remote terlebih dahulu!")
    
    def on_v_press(self, e):
        if self.is_active:
            self.press_vol_up()
    
    def flash_button(self, button):
        original_bg = button.cget('bg')
        button.config(bg='#FFFF00')
        self.root.after(100, lambda: button.config(bg=original_bg))

def main():
    root = tk.Tk()
    app = RemoteTVApp(root)
    root.mainloop()

if __name__ == "__main__":
    main()
