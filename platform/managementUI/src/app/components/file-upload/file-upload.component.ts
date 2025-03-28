import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Utils } from 'src/app/services/utils.service';

/**
 * Component providing a file selection/upload button.
 */
@Component({
    selector: 'app-file-upload',
    templateUrl: './file-upload.component.html',
    styleUrls: ['./file-upload.component.scss'],
    standalone: false
})
export class FileUploadComponent extends Utils {

  /**
   * File name produced by clicking on browser file selection dialog.
   */
  fileName = '';
  /**
   * File name filter passed on to browser.
   */
  @Input() accept = '*.*';
  /**
   * Passed on event to parent, bound by (result).
   */
  @Output() result = new EventEmitter<File>();
  @Input() enabled = true;
  @Input() tooltip = "Upload";

  constructor() {
    super();
  }

  /**
   * Called when file is selected.
   * 
   * @param event the event produced by the input element
   */
  onFileSelected(event: any) {
    if (this.enabled) {
      const file:File = event.target.files[0];
      if (file) {
          this.fileName = file.name;
          this.result.emit(file);
      }
    }
  }

}

/**
 * Chunks the input.
 * 
 * @param info the file to upload
 * @param chunkSize the desired chunk size (non-positive will become 1)
 * @param fn function to call per chunk - chunk data and sequence number; if chunk data is null, error occurred. if sequen
 *   number is 0, entire file is in chunk. If sequence number is positive, this is one chunk and further will follow. If
 *   sequence number is negative, this is the last chunk indicating  the sequence number if turned to absolute
 */
export async function chunkInput(file: File, chunkSize: number, chunkFn: (chunk: ChunkType, seqNr: number) => void, 
  completedFn: (success: boolean) => void) {
  var offset = 0;
  var seqNr = 0;
  chunkSize = Math.max(chunkSize, 1); // just to avoid endless looks
  var fr = new FileReader();
  fr.onload = () => {
    var chunk: ArrayBuffer = fr.result as ArrayBuffer;
    var nr = seqNr;
    if (chunk) {
      if (chunk.byteLength < chunkSize || offset + chunkSize == file.size) { // end of data
        nr = -nr;
      } else if (seqNr == 0) { // it's the first but filled up, increase sequence number to go on
        seqNr = 1;
        nr = seqNr;
      }
    } else { // failure, terminate and do not seek further on
      nr = -nr;
      completedFn(false);
    }
    chunkFn(chunk, nr); // notify processing
    if (nr > 0) { // not single chunk, not end of chunks -> go on
      offset += chunkSize;
      seqNr++;
      seek(offset, file, chunkSize, fr); // subsequent seek
    } else {
      completedFn(true);
    }
  };
  fr.onerror = () => {
    chunkFn(null, 0);
    completedFn(false);
  };
  seek(offset, file, chunkSize, fr); // first seek
}

/**
 * Helper function for chunking input.
 * 
 * @param offset actual offset within file
 * @param file file to read
 * @param chunkSize size of chunk to advance
 * @param fr file reader
 */
function seek(offset: number, file: File, chunkSize: number, fr: FileReader) {
  var slice = file.slice(offset, offset + chunkSize);
  fr.readAsArrayBuffer(slice);
}

export type ChunkType = ArrayBuffer | null;
