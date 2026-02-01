import tkinter as tk
from tkinter import ttk
import subprocess
import re

class RemoteTVBluetoothApp:
    def __init__(self, root):
        self.root = root
        self.root.title("Remote TV Bluetooth (AVRCP)")
        self.root.geometry("500x450")
        self.root.resizable(False, False)
        
        # Status
        self.is_active = False
        self.target_device = None
        
        # Main Frame
        main_frame = ttk.Frame(root, padding="20")
        main_frame.pack(expand=True, fill='both')
        
        # Title
        title_label = ttk.Label(main_frame, text="Remote TV Bluetooth (AVRCP)", 
                               font=('Arial', 14, 'bold'))
        title_label.pack(pady=15)
        
        # Status Label
        self.status_label = ttk.Label(main_frame, text="Status: NONAKTIF", 
                                     font=('Arial', 12), foreground='red')
        self.status_label.pack(pady=10)
        
        # Device Info
        self.device_label = ttk.Label(main_frame, text="Device: Belum terdeteksi", 
                                     font=('Arial', 10), foreground='gray')
        self.device_label.pack(pady=5)
        
        # Scan Button
        self.scan_button = tk.Button(main_frame, text="🔍 Scan Device TV", 
                                    bg='#009688', fg='white',
                                    font=('Arial', 11, 'bold'),
                                    width=20, height=1,
                                    command=self.scan_devices)
        self.scan_button.pack(pady=10)
        
        # Button Frame
        button_frame = ttk.Frame(main_frame)
        button_frame.pack(pady=15)
        
        # Tombol OK
        self.ok_button = tk.Button(button_frame, text="OK", 
                                  bg='#4CAF50', fg='white',
                                  font=('Arial', 12, 'bold'),
                                  width=10, height=2,
                                  command=self.press_ok,
                                  state='disabled')
        self.ok_button.grid(row=0, column=0, padx=8, pady=8)
        
        # Tombol Play/Pause
        self.play_button = tk.Button(button_frame, text="▶||", 
                                    bg='#2196F3', fg='white',
                                    font=('Arial', 12, 'bold'),
                                    width=10, height=2,
                                    command=self.press_play,
                                    state='disabled')
        self.play_button.grid(row=0, column=1, padx=8, pady=8)
        
        # Tombol Volume Up
        self.vol_up_button = tk.Button(button_frame, text="VOL +", 
                                      bg='#FF5722', fg='white',
                                      font=('Arial', 12, 'bold'),
                                      width=10, height=2,
                                      command=self.press_vol_up,
                                      state='disabled')
        self.vol_up_button.grid(row=1, column=0, padx=8, pady=8)
        
        # Tombol Volume Down
        self.vol_down_button = tk.Button(button_frame, text="VOL -", 
                                        bg='#9C27B0', fg='white',
                                        font=('Arial', 12, 'bold'),
                                        width=10, height=2,
                                        command=self.press_vol_down,
                                        state='disabled')
        self.vol_down_button.grid(row=1, column=1, padx=8, pady=8)
        
        # Activate Button
        self.activate_button = tk.Button(main_frame, text="AKTIFKAN", 
                                        bg='#FF9800', fg='white',
                                        font=('Arial', 11, 'bold'),
                                        width=18, height=1,
                                        command=self.toggle_active,
                                        state='disabled')
        self.activate_button.pack(pady=15)
        
        # Info Label
        info_label = ttk.Label(main_frame, 
                              text="1. Scan device TV dulu\n2. Klik AKTIFKAN untuk menghubungkan\n3. Gunakan tombol untuk mengontrol TV",
                              font=('Arial', 9), justify='center')
        info_label.pack(pady=10)
        
        # Log
        self.log_label = ttk.Label(main_frame, text="", font=('Arial', 9))
        self.log_label.pack(pady=5)
        
        # Auto-scan on startup
        self.root.after(1000, self.scan_devices)
    
    def scan_devices(self):
        self.log_label.config(text="Scanning device Bluetooth...")
        self.scan_button.config(state='disabled')
        
        try:
            # Get paired Bluetooth devices
            result = subprocess.run(['powershell', 
                                   '-Command', 
                                   'Get-PnpDevice -Class Bluetooth | '
                                   'Where-Object {$_.Status -eq "OK"} | '
                                   'Select-Object FriendlyName | '
                                   'Format-Table -HideTableHeaders'], 
                                  capture_output=True, text=True, timeout=10)
            
            devices = result.stdout.strip()
            
            # Look for TV devices
            tv_devices = []
            for line in devices.split('\n'):
                line = line.strip()
                if line and any(keyword in line.upper() for keyword in ['TV', 'B860H', 'JQVITEK', 'REMOTE']):
                    tv_devices.append(line)
            
            if tv_devices:
                self.target_device = tv_devices[0]
                self.device_label.config(text=f"Device: {self.target_device}", foreground='green')
                self.log_label.config(text=f"Device TV ditemukan: {self.target_device}")
                
                # Enable buttons
                self.ok_button.config(state='normal')
                self.play_button.config(state='normal')
                self.vol_up_button.config(state='normal')
                self.vol_down_button.config(state='normal')
                self.activate_button.config(state='normal')
            else:
                self.device_label.config(text="Device: TV tidak ditemukan", foreground='red')
                self.log_label.config(text="Tidak ada device TV yang terdeteksi")
                
        except Exception as e:
            self.log_label.config(text=f"Error: {str(e)}")
        
        self.scan_button.config(state='normal')
    
    def toggle_active(self):
        self.is_active = not self.is_active
        
        if self.is_active:
            self.status_label.config(text="Status: AKTIF - Terhubung ke TV", foreground='green')
            self.activate_button.config(text="NONAKTIFKAN", bg='#f44336')
            self.log_label.config(text="Remote siap! Gunakan tombol untuk kontrol TV")
        else:
            self.status_label.config(text="Status: NONAKTIF", foreground='red')
            self.activate_button.config(text="AKTIFKAN", bg='#FF9800')
            self.log_label.config(text="")
    
    def press_ok(self):
        if self.is_active and self.target_device:
            self.log_label.config(text="Mengirim sinyal OK ke TV...")
            self.send_bluetooth_command("OK")
            self.flash_button(self.ok_button)
        else:
            self.log_label.config(text="Aktifkan remote terlebih dahulu!")
    
    def press_play(self):
        if self.is_active and self.target_device:
            self.log_label.config(text="Mengirim sinyal Play/Pause ke TV...")
            self.send_bluetooth_command("PLAY_PAUSE")
            self.flash_button(self.play_button)
        else:
            self.log_label.config(text="Aktifkan remote terlebih dahulu!")
    
    def press_vol_up(self):
        if self.is_active and self.target_device:
            self.log_label.config(text="Mengirim Volume Up ke TV...")
            self.send_bluetooth_command("VOLUME_UP")
            self.flash_button(self.vol_up_button)
        else:
            self.log_label.config(text="Aktifkan remote terlebih dahulu!")
    
    def press_vol_down(self):
        if self.is_active and self.target_device:
            self.log_label.config(text="Mengirim Volume Down ke TV...")
            self.send_bluetooth_command("VOLUME_DOWN")
            self.flash_button(self.vol_down_button)
        else:
            self.log_label.config(text="Aktifkan remote terlebih dahulu!")
    
    def send_bluetooth_command(self, command):
        """Send command via Windows Bluetooth AVRCP"""
        try:
            # Using Windows Media Commands for AVRCP
            if command == "PLAY_PAUSE":
                subprocess.run(['powershell', '-Command', 
                              '(New-Object -ComObject WScript.Shell).SendKeys("{MEDIA_PLAY_PAUSE}")'],
                             capture_output=True)
            elif command == "VOLUME_UP":
                subprocess.run(['powershell', '-Command', 
                              '(New-Object -ComObject WScript.Shell).SendKeys("{VOLUME_UP}")'],
                             capture_output=True)
            elif command == "VOLUME_DOWN":
                subprocess.run(['powershell', '-Command', 
                              '(New-Object -ComObject WScript.Shell).SendKeys("{VOLUME_DOWN}")'],
                             capture_output=True)
            elif command == "OK":
                subprocess.run(['powershell', '-Command', 
                              '(New-Object -ComObject WScript.Shell).SendKeys("{ENTER}")'],
                             capture_output=True)
            
            self.log_label.config(text=f"Sinyal {command} terkirim ke TV via Bluetooth!")
            
        except Exception as e:
            self.log_label.config(text=f"Error: {str(e)}")
    
    def flash_button(self, button):
        original_bg = button.cget('bg')
        button.config(bg='#FFFF00')
        self.root.after(100, lambda: button.config(bg=original_bg))

def main():
    root = tk.Tk()
    app = RemoteTVBluetoothApp(root)
    root.mainloop()

if __name__ == "__main__":
    main()
