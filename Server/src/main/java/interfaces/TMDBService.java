package interfaces;

import model.data.DefaultData;

public interface TMDBService {

      Data search(String title);
      Data findWithID(int id);

}
