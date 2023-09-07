# IIP-Ecosphere platform: Beckhoff TwinCat ADS integration library

Java integration of the Beckhoff TwinCat ADS library via JNA. Ships with a linux library taken from pyads, but 
**without** the Windows version due to IPR/license issues. ``-Diip.libs.ads`` defines the path to read the native libraries from, e.g., where ``TcsAds.dll`` is located can be set. Make sure to point it into the directory containing the ``.dll``.