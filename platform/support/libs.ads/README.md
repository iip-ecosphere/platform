# IIP-Ecosphere platform: Beckhoff TwinCat ADS integration library

Java integration of the Beckhoff TwinCat ADS library via JNA. Ships with a linux library taken from pyads, but 
**without** the Windows version due to IPR/license issues. ``-Diip.libs.ads`` defines the basic path to read the native libraries from. Within this folder, the implementation searches for sub-folders specific to the operating system, i.e., `win32-x86-32`, `win32-x86-64` or `linux-x86-64`, in which the respective ``TcsAds.dll`` or ``adslib.so`` file is located.

If loading the library fails, you may set ``-Djna.debug_load=true`` for debugging.

So far, tested mainly for Windows. However, junit tests without a Beckhoff Environment are rather useless.
