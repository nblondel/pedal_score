package queues;

import java.util.ArrayList;
import java.util.List;

public class NoteBuffer {
	private List<Note> notes = new ArrayList<Note>();
	private List<Note> referenceNotes = null;

	public NoteBuffer(List<Note> referenceNotes) {
		this.referenceNotes = referenceNotes;
	}

	public void addNote(double frequency, double time) {
		Note newPitch = new Note(frequency, time);
		notes.add(newPitch);
	}

	public int size() {
		return notes.size();
	}

	public double[] getFrequencyArray() {
		double[] frequencies = new double[notes.size()];
		for (int i = 0; i < notes.size(); i++) {
			frequencies[i] = notes.get(i).getFrequency();
		}

		return frequencies;
	}

	public double[] getTimeArray() {
		double[] times = new double[notes.size()];
		for (int i = 0; i < notes.size(); i++) {
			times[i] = notes.get(i).getTime();
		}

		return times;
	}

	public String[] getNamesArray() {
		String[] names = new String[notes.size()];
		for (int i = 0; i < notes.size(); i++) {
			names[i] = notes.get(i).getName();
		}

		return names;
	}

	public int[] getOctavesArray() {
		int[] octaves = new int[notes.size()];
		for (int i = 0; i < notes.size(); i++) {
			octaves[i] = notes.get(i).getOctave();
		}

		return octaves;
	}

	public boolean[] getHiddenArray() {
		boolean[] hiddens = new boolean[notes.size()];
		for (int i = 0; i < notes.size(); i++) {
			hiddens[i] = notes.get(i).isHidden();
		}

		return hiddens;
	}

	public void clear() {
		notes.clear();
	}

	public void computeRealNotes() {
		for (Note note : notes) {

			/* Find the note that looks like the pitch */
			double freqDifference = note.getFrequency(); // Start with a difference to keep resizing to smaller and
															// smaller until we find the smallest

			// Keep pitch as note to start
			double currentFrequency = note.getFrequency();
			String currentNoteName = "Unknown";
			int currentNoteOctave = 0;

			for (int index = 0; index < referenceNotes.size(); index++) {
				double newFreqDifference = Math.abs(note.getFrequency() - referenceNotes.get(index).getFrequency());
				if (newFreqDifference > freqDifference)
					break;

				freqDifference = newFreqDifference;
				currentFrequency = referenceNotes.get(index).getFrequency();
				currentNoteName = referenceNotes.get(index).getName();
				currentNoteOctave = referenceNotes.get(index).getOctave();
			}

			/* Create note from the reference notes */
			note.setName(currentNoteName);
			note.setFrequency(currentFrequency);
			note.setOctave(currentNoteOctave);
		}
	}

	/**
	 * Create note from pitches
	 * 
	 * @param pitchBufferCopy
	 */
	public void setRawNotesFromPitchBuffer(PitchBuffer pitchBufferCopy) {
		for (Pitch pitch : pitchBufferCopy.values()) {
			/* Create note from the pitch */
			double frequency = (double) Math.round(pitch.getFrequency());
			if (pitch.isInhibited()) {
				frequency = 0.0;
			}
			Note newNote = new Note(frequency, pitch.getTime());
			/* Save this note */
			notes.add(newNote);
		}
	}

	/**
	 * Set the attribute 'Hidden' of the Note true if the duration of the note (a
	 * set of pitches) duration is smaller than the minimum duration
	 * 
	 * @param minimum_duration The minimum duration (milliseconds)
	 */
	public void hideSmallDurations(double minimum_duration, int ignored_allowed) {
		int index = 0;
//    System.out.println("Notes:");
//    /* Create notes from pitch (set the real notes frequencies) */
//    for(index = 0; index < notes.size(); index++) {
//      notes.get(index).setFrequency((double)Math.round(notes.get(index).getFrequency()));
//      System.out.println("Note " + index + ": " + notes.get(index).getFrequency());
//    }
//    System.out.println();

		/* Remove small durations */
		index = 0;
		while (index < notes.size()) {
			int same_frequency_index = index;

			/* Find same note frequencies */
			boolean search_same_notes_loop = true; /* Main loop condition */
			int ignored_amount = 0; /* The total of notes ignored, cannot be superior to 10 */
			boolean init_ignored_notes = false; /*
												 * Initialize the ignored notes when at least 5 sames notes are
												 * following each other
												 */
			boolean notes_ignored_confirmed = false; /* Notes cannot be ignored more than once */
			boolean at_least_two_consecutives = false; /* At least 2 consecutive same frequency for time comparison */
			int consecutive_notes_counter = 1; /* Consecutive notes counter */
			int first_ignored_note_index = 0; /* The index of the first ignored note */
			double last_correct_frequency = 0.0; /* Frequency of the last non-ignored note */

			do {
				/* Check for two consecutive notes with the same frequency */
				if ((same_frequency_index + 1) >= notes.size()) {
					/* End of the notes buffer */
					search_same_notes_loop = false;
				} else {
					if (!notes.get(index).equals(notes.get(same_frequency_index + 1))) {
						/* Different notes: accept up to 10 differences */
						if (notes_ignored_confirmed) {
							/* Notes are already been ignored, stop the research */
							search_same_notes_loop = false;
						} else {
							/* First ignored notes, reset the consecutive notes counter */
							consecutive_notes_counter = 1;
							if (init_ignored_notes) {
								/*
								 * If the notes ignoring is enabled, start to ignore notes that are not equals
								 * to the previous ones
								 */
								first_ignored_note_index = same_frequency_index + 1;
								ignored_amount++;
								if (ignored_amount > ignored_allowed) {
									/* Too many ignored, stop the process */
									search_same_notes_loop = false;
								}
							} else {
								/* Ignore process not allowed, stop the process */
								search_same_notes_loop = false;
							}
						}
					} else {
						/* Two same consecutive notes, allow to compare their time */
						at_least_two_consecutives = true;
						last_correct_frequency = notes.get(same_frequency_index + 1).getFrequency();

						consecutive_notes_counter++;
						if (!init_ignored_notes && consecutive_notes_counter >= 5) {
							/* First same 5 notes, init ignored notes */
							init_ignored_notes = true;
						}

						if (ignored_amount > 0) {
							/*
							 * Some notes have already been ignored and some consecutive notes are also
							 * found after the ignored one: disable to allow ignored notes again
							 */
							notes_ignored_confirmed = true;
						}
						// System.out.println(index + " is the same as " + (same_frequency_index + 1));
					}

					same_frequency_index++;
				}
			} while (search_same_notes_loop);

			if (ignored_amount > 0) {
				/* Some notes have been ignored */
				if (notes_ignored_confirmed == false) {
					/* Do not take the ignored in account */
					same_frequency_index -= ignored_amount;
				} else {
					for (int ignored_index = first_ignored_note_index; ignored_index < (first_ignored_note_index
							+ ignored_amount); ignored_index++) {
						notes.get(ignored_index).setFrequency(last_correct_frequency);
					}
				}
			}

			if (at_least_two_consecutives && same_frequency_index < notes.size()) {
				// System.out.println("Keep notes from " + index + " to " +
				// same_frequency_index);

				/* Compute the duration of this same frequency */
				double duration_milliseconds = (notes.get(same_frequency_index).getTime() * 1000.0
						- notes.get(index).getTime() * 1000.0);
				// System.out.println("Duration: " + duration_milliseconds);
				if (duration_milliseconds <= minimum_duration) {
					for (int notes_index = index; notes_index < same_frequency_index; notes_index++) {
						/* Duration too small, hide all these notes */
						// System.out.println("note " + notes_index + " hidden for small duration
						// ("+notes.get(notes_index).getFrequency()+")");
						notes.get(notes_index).setHidden(true);
					}
				}
				index = same_frequency_index + 1;
			} else {
				/* There is not even 2 notes with the same frequency */
				// System.out.println("note " + index + " hidden not similar to its neighbour
				// ("+notes.get(index).getFrequency()+")");
				notes.get(index).setHidden(true);
				index++;
			}
		}
	}
}
