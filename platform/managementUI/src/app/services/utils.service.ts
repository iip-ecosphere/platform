import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class UtilsService {

  constructor() { }
}

export interface RetryProps {
  attempts?: number
  delay: number
  fn: () => boolean
  maxAttempts: number
}

export function retry({ fn, maxAttempts = 1, delay = 1000, attempts = 1 }: RetryProps) {
  return new Promise((resolve, reject) => {
    if (fn()) resolve(true)
    else {
      if (attempts < maxAttempts) {
        setTimeout(
          () =>
            retry({ fn, maxAttempts, delay, attempts: attempts + 1 })
              .then(() => resolve(true))
              .catch((err) => reject(err)),
          delay
        )
      } else reject('Could not finally resolve promise - retry failed.')
    }
  })
}
