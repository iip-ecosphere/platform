import logging as logger
logger.basicConfig(level="DEBUG")
import re

SEPARATOR = "."
PATTERN = re.compile("^(\d+)(\.\d+)*$")

def isVersion(version):
    """Returns whether the given value is a string in form version format.
    
    Parameters:
      - version -- the value to be checked
      
    Returns:
      bool
        True if version is a string and matches PATTERN
        False else
    """
    
    if isinstance(version, str):
        if PATTERN.match(version):
            return True
        else:
            return False
    else:
        return False
        
def compare(version1, version2):
    """Compares two given versions. Considers "is None".
    
    Parameters:
      - version1 -- the first version to be compared
      - version2 -- the second version to be compared
      
    Returns:
      int
        -1 if version1 is smaller than version2, 1 if version1 
        is greater than version2, 0 if version1 and version2 are equal
    """
    
    # taken over from Java for same semantics, could use equals
    if version1 is None:
        if version2 is None:
            return 0
        else: 
            return -1
    elif version2 is None:
        return 1
    else: 
        return version1.compareTo(version2);

def equals(version1, version2):
    """Compares two given versions for equality. Considers "is None".
    
    Parameters:
      - version1 -- the first version to be compared
      - version2 -- the second version to be compared
      
    Returns:
      bool
        True if both versions are equal, False else
    """

    # taken over from Java for same semantics, could use equals
    if version1 is None:
        return version2 is None
    else:
        if not(version2 is None):
            return 0 == version1.compareTo(version2)
        else:
            return False

class Version:
    """Represents a version as in IIP-Ecosphere de.iip_ecosphere.platform.support.iip_aas."""
    
    def __init__(self, version:str):
        """Initializes the service.
        
        Parameters:
          - version -- the textual version in format "\d(.\d+)"
        """ 
        if isVersion(version):
            self.segments = list(map(int, filter(str.isdigit, re.split(r'(\d+)', version))))
        else:
            self.segments = [0]

    def getSegmentCount(self):
        """Returns the number of segments of this version.
        
        Returns:
          int 
            The number of version segments
        """
        
        return len(self.segments)

    def getSegment(self, index):
        """Returns the value of the given segment.
        
        Parameters:
          - index --- the index of the segment in [0; getSegmentCount()]
        
        Returns:
          int 
            The value of the segment.
        """
        
        return self.segments[index]
        
    def toString(self):
        """Turns this version into a canonical string representation.
        
        Returns:
          str 
            The string representation in format "\d(.\d+)"
        """

        return '.'.join(map(str, self.segments))
    
    def compareTo(self, version):
        """Compares this and a given version.
        
        Parameters:
          - version -- the version to compare against this version
          
        Returns:
          int
            -1 if self is smaller than version, 1 if self 
            is greater than version, 0 if self and version are equal
        """

        # not nice, taken over from Java for same semantics
        unset = 3
        result = unset
        segmentCount = min([self.getSegmentCount(), version.getSegmentCount()])
        
        if result == unset:
            i = 0
            while i < segmentCount:
                if self.getSegment(i) > version.getSegment(i):
                    result = 1
                    break
                elif self.getSegment(i) < version.getSegment(i):
                    result = -1
                    break
                i += 1
        if result == unset and version.getSegmentCount() < self.getSegmentCount():
            result = 1
        elif result == unset and version.getSegmentCount() > self.getSegmentCount():
            result = -1
        elif result == unset:
            result = 0

        return result
        
    def equals(self, version):
        """Compares this version against the given version for equality. Considers "is None".
        
        Parameters:
          - version -- the version to compare against
          
        Returns:
          bool
            True if both versions are equal, False else
        """
    
        if isinstance(version, Version):
            return self.segments == version.segments
        else:
            return False
            
    def __str__(self):
        """Returns the string representation for print.
        
        Returns:
          str
            the String representation for print via toString()
        """
        return self.toString()